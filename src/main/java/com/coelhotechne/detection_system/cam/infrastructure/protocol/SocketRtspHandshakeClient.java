package com.coelhotechne.detection_system.cam.infrastructure.protocol;

import com.coelhotechne.detection_system.cam.api.dto.RtspResponse;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionProfile;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionTestProbe;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.cam.domain.homologation.CamConnectionTestOutcome;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Component
@EnableConfigurationProperties(CamProbeProperties.class)
public class SocketRtspHandshakeClient implements CamConnectionTestProbe {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String USER_AGENT = "CoelhoTechne-CamHomologation/1.0";
    private static final String RTSP_SCHEME = "rtsp";
    private static final int DEFAULT_RTSP_PORT = 554;
    private static final int MAX_BODY_LENGTH = 64 * 1024;

    private final CamCredentialResolver credentialResolver;
    private final CamProbeProperties probeProperties;

    public SocketRtspHandshakeClient(CamCredentialResolver credentialResolver,CamProbeProperties probeProperties) {
        this.credentialResolver = credentialResolver;
        this.probeProperties = probeProperties;
    }

    @Override
    public CamConnectionTestOutcome test(CamConnectionProfile connection) {
        if (connection.getProtocol() != CamProtocol.RTSP && connection.getProtocol() != CamProtocol.NATIVE) {
            return CamConnectionTestOutcome.failure(
                    "SocketRtspHandshakeClient only supports RTSP/NATIVE, received: " + connection.getProtocol());
        }
        StreamTarget target;
        try {
            target = parseStreamUri(connection.getStreamUri());
        } catch (InvalidStreamUriException e) {
            return CamConnectionTestOutcome.failure(e.getMessage());
        }
        InetAddress address;
        try {
            address = InetAddress.getByName(target.host());
        }catch (UnknownHostException ex){
            return CamConnectionTestOutcome.failure("Unknown host: "+target.host);
        }
        String rejection = probeProperties.rejectionReason(address);
        if (rejection!=null){
            return  CamConnectionTestOutcome.failure("Destination not allowed ("+rejection+")");
        }
        target = target.withAddress(address);

        try (RtspCredentials credentials = resolveCredentials(connection, target.host())) {
            String endpoint = target.host() + ":" + target.port();

            try {
                RtspResponse first = describe(target, 1, null);
                if (first.statusCode() == 200) {
                    return CamConnectionTestOutcome.success(SdpMetadataExtractor.extractCodec(first.body()));
                }
                if (first.statusCode() != 401) {
                    return CamConnectionTestOutcome.failure("RTSP DESCRIBE returned status " + first.statusCode());
                }

                List<String> challenges = first.header("WWW-Authenticate");
                if (challenges.isEmpty()) {
                    return CamConnectionTestOutcome
                            .failure("Camera returned 401 without WWW-Authenticate header");
                }
                if (credentials == null) {
                    return CamConnectionTestOutcome.failure(
                            "Camera requires authentication, but no credentials were resolved (credentialRef="
                                    + connection.getCredentialRef() + ")");
                }

                Optional<String> digest = challenges.stream()
                        .filter(c -> c.regionMatches(true, 0, "digest", 0, 6))
                        .findFirst();
                if (digest.isEmpty() && !credentials.allowBasic()){
                    return CamConnectionTestOutcome.failure("Camera offers only Basic authentication; enable cam.credentials."
                    + connection.getCredentialRef()+".allow-basic=true to send it");
                }
                String challenge = digest.orElse(challenges.get(0));
                String authorization = buildAuthorizationHeader(challenge, credentials, target.requestUri(), "DESCRIBE");
                // Alguns servidores de RTSP fecham a conexão após responder 401 — nova conexão pro retry:
                RtspResponse second = describe(target, 2, authorization);
                if (second.statusCode() == 200) {
                    return CamConnectionTestOutcome.success(SdpMetadataExtractor.extractCodec(second.body()));
                }
                if (second.statusCode() == 401) {
                    return CamConnectionTestOutcome.failure("Credentials rejected by the camera (401 after authentication)");
                }
                return CamConnectionTestOutcome.failure(
                        "Authenticated RTSP DESCRIBE returned status " + second.statusCode());

            } catch (SocketTimeoutException e) {
                return CamConnectionTestOutcome.failure("Timeout communicating with: " + endpoint);
            } catch (IOException e) {
                return CamConnectionTestOutcome.failure("Connection failed with: " + endpoint + ": " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                return CamConnectionTestOutcome.failure("Digest calculation failed (MD5 unavailable): " + e.getMessage());
            }
        }

    }

    // ---------------------------------------------------------------------
    // Validação da URI
    // ---------------------------------------------------------------------

    private record StreamTarget(URI uri, String host, int port,InetAddress address) {
        String requestUri() {
            return uri.toASCIIString();
        }
        StreamTarget withAddress(InetAddress resolved){
            return new StreamTarget(uri,host,port,resolved);
        }
    }

    private static class InvalidStreamUriException extends Exception {
        InvalidStreamUriException(String message) {
            super(message);
        }
    }

    // ---------------------------------------------------------------------
    // Credenciais
    // ---------------------------------------------------------------------

    private static class CredentialResolutionException extends Exception {
        CredentialResolutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private RtspCredentials resolveCredentials(CamConnectionProfile camConnectionProfile,String host){
        if (camConnectionProfile.getCredentialRef() == null){
            return null;
        }
        try {
            return credentialResolver.resolve(camConnectionProfile.getCredentialRef(),host);
        } catch (RuntimeException e) {
            log.warn("Credential resolution failed for credentialRef={}", camConnectionProfile.getCredentialRef(), e);
            return null;
        }
    }
    private StreamTarget parseStreamUri(String streamUri) throws InvalidStreamUriException {
        if (streamUri == null || streamUri.isBlank()) {
            throw new InvalidStreamUriException("Stream URI not configured");
        }

        URI uri;
        try {
            uri = new URI(streamUri.trim());
        } catch (URISyntaxException e) {
            // getReason() não inclui a string original — se atentar a vazamentos na mensagem
            throw new InvalidStreamUriException("Invalid stream URI: " + e.getReason());
        }

        if (!RTSP_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            throw new InvalidStreamUriException(
                    "Unsupported scheme: " + uri.getScheme() + " (expected " + RTSP_SCHEME + ")");
        }

        // authority bruta: getUserInfo() pode voltar null quando o Java não consegue
        // interpretar a authority como host:porta, mesmo com "user:pass@" presente
        String authority = uri.getRawAuthority();
        if (authority != null && authority.contains("@")) {
            throw new InvalidStreamUriException("Stream URI must not contain credentials; use credentialRef");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new InvalidStreamUriException("Stream URI has no valid host");
        }

        int port = uri.getPort() == -1 ? DEFAULT_RTSP_PORT : uri.getPort();
        return new StreamTarget(uri, host, port,null);
    }

    // ---------------------------------------------------------------------
    // Transporte RTSP
    // ---------------------------------------------------------------------

    private RtspResponse describe(StreamTarget target, int cseq, String authorizationHeader) throws IOException {
        try (Socket socket = new Socket()) {
            InetSocketAddress endpoint = target.address()!=null
                    ? new InetSocketAddress(target.address(),target.port())
                    : new InetSocketAddress(target.host(), target.port());
            socket.connect(endpoint,(int) TIMEOUT.toMillis());
            socket.setSoTimeout((int) TIMEOUT.toMillis());

            StringBuilder request = new StringBuilder();
            request.append("DESCRIBE ").append(target.requestUri()).append(" RTSP/1.0\r\n");
            request.append("CSeq: ").append(cseq).append("\r\n");
            request.append("Accept: application/sdp\r\n");
            request.append("User-Agent: ").append(USER_AGENT).append("\r\n");
            if (authorizationHeader != null) {
                request.append("Authorization: ").append(authorizationHeader).append("\r\n");
            }
            request.append("\r\n");

            OutputStream out = socket.getOutputStream();
            out.write(request.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

            return parseResponse(socket.getInputStream());
        }
    }

    private RtspResponse parseResponse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String statusLine = reader.readLine();
        if (statusLine == null) {
            throw new EOFException("Connection closed before response");
        }
        // ex.: "RTSP/1.0 200 OK"
        String[] parts = statusLine.split(" ", 3);
        if (parts.length < 2 || !parts[0].startsWith("RTSP/")) {
            throw new ProtocolException("Not an RTSP response: " + statusLine);
        }
        int statusCode = parseIntOrThrow(parts[1], "status code");

        Map<String, List<String>> headers = new LinkedHashMap<>();
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int sep = line.indexOf(':');
            if (sep <= 0) {
                continue;
            }
            String name = line.substring(0, sep).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(sep + 1).trim();
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);

            if (name.equals("content-length")) {
                contentLength = parseIntOrThrow(value, "Content-Length");
                if (contentLength < 0 || contentLength > MAX_BODY_LENGTH) {
                    throw new ProtocolException("Content-Length out of bounds: " + contentLength);
                }
            }
        }

        String body = null;
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int n = reader.read(buffer, read, contentLength - read);
                if (n == -1) {
                    break;
                }
                read += n;
            }
            body = new String(buffer, 0, read);
        }

        return new RtspResponse(statusCode, headers, body);
    }

    private int parseIntOrThrow(String value, String field) throws ProtocolException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid " + field + ": " + value);
        }
    }

    // ---------------------------------------------------------------------
    // Autenticação
    // ---------------------------------------------------------------------

    private String buildAuthorizationHeader(String challenge, RtspCredentials credentials, String uri, String method)
            throws NoSuchAlgorithmException {
        if (challenge.regionMatches(true, 0, "digest", 0, 6)) {
            return buildDigestHeader(challenge, credentials, uri, method);
        }
        return buildBasicHeader(credentials);
    }

    // Monta "usuário:senha" em bytes (sem String intermediária com a senha),
    // codifica em Base64 e zera os buffers. O header resultante ainda contém a
    // senha de forma reversível — limitação do próprio esquema Basic.
    private String buildBasicHeader(RtspCredentials credentials) {
        byte[] prefix = (credentials.username() + ":").getBytes(StandardCharsets.UTF_8);
        byte[] password = credentials.passwordUtf8();
        byte[] raw = new byte[prefix.length + password.length];
        try {
            System.arraycopy(prefix, 0, raw, 0, prefix.length);
            System.arraycopy(password, 0, raw, prefix.length, password.length);
            return "Basic " + Base64.getEncoder().encodeToString(raw);
        } finally {
            Arrays.fill(password, (byte) 0);
            Arrays.fill(raw, (byte) 0);
        }
    }

    // RFC 2617 — Digest sem qop (compatível com a maioria das câmeras IP baratas, que ainda
    // implementam o esquema antigo do RFC 2069/2617 sem "qop=auth"). Câmeras que exigem
    // qop=auth (com nc/cnonce) vão precisar de extensão deste metodo.
    private String buildDigestHeader(String challenge, RtspCredentials credentials, String uri, String method)
            throws NoSuchAlgorithmException {
        String realm = extractDirective(challenge, "realm");
        String nonce = extractDirective(challenge, "nonce");

        String ha1 = digestHa1(credentials, realm);
        String ha2 = md5(method + ":" + uri);
        String response = md5(ha1 + ":" + nonce + ":" + ha2);

        return "Digest username=\"" + credentials.username() + "\", realm=\"" + realm + "\", nonce=\"" + nonce
                + "\", uri=\"" + uri + "\", response=\"" + response + "\"";
    }

    // HA1 = MD5(usuário:realm:senha). A senha entra só como bytes e é zerada
    // logo após o uso. O HA1 equivale à senha para fins de autenticação.
    private String digestHa1(RtspCredentials credentials, String realm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update((credentials.username() + ":" + realm + ":").getBytes(StandardCharsets.UTF_8));
        byte[] password = credentials.passwordUtf8();
        try {
            md.update(password);
        } finally {
            Arrays.fill(password, (byte) 0);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    private String extractDirective(String header, String directive) {
        Pattern pattern = Pattern.compile(directive + "=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(header);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String md5(String input) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}