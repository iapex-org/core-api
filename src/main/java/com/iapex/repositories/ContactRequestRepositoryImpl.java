package com.iapex.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.iapex.dtos.contactRequest.ContactRequestCount;

@Repository
public class ContactRequestRepositoryImpl {
    
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ContactRequestRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ContactRequestCount> getContactRequestStatusCount() {
        return jdbcTemplate.query(
            "SELECT * FROM get_contact_request_status_count()",
            (rs, rowNum) -> new ContactRequestCount(
                rs.getString("status"),
                rs.getLong("count")
            )
        );
    }

    public List<ContactRequestCount> getContactRequestStatusCountByInstitution(Long institutionId) {
        return jdbcTemplate.query(
            "SELECT * FROM get_contact_request_status_count_by_institution(?)",
            new Object[]{institutionId},
            (rs, rowNum) -> new ContactRequestCount(
                rs.getString("status"),
                rs.getLong("count")
            )
        );
    }
}
