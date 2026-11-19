package com.coelhotechne.detection_system.cluster.infrastructure;

import com.coelhotechne.detection_system.cluster.domain.ClusterLease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ClusterLeaseRepository extends JpaRepository<ClusterLease, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ClusterLease l
               set l.owner = :owner, l.lockedAt = :now, l.lockedUntil = :until
             where l.name = :name and (l.lockedUntil <= :now or l.owner = :owner)
            """)
    int tryTake(
            @Param("name")
            String name,
            @Param("owner")
            String owner,
            @Param("now")
            Instant now,
            @Param("until")
            Instant until
    );
    @Modifying
    @Query(value = """
            insert into cluster_lease (name, owner, locked_at, locked_until)
            values (:name, :owner, :now, :until)
            """, nativeQuery = true)
    int insertNew(@Param("name")
                  String name,
                  @Param("owner")
                  String owner,
                  @Param("now")
                  Instant now,
                  @Param("until")
                  Instant until);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ClusterLease l set l.lockedUntil = :until
             where l.name = :name and l.owner = :owner
            """)
    int release(
            @Param("name")
            String name,
            @Param("owner")
            String owner,
            @Param("until")
            Instant until
    );
}
