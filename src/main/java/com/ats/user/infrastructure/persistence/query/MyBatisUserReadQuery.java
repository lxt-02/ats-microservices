package com.ats.user.infrastructure.persistence.query;

import com.ats.shared.infrastructure.mybatis.LikePatterns;
import com.ats.shared.web.PageResponse;
import com.ats.user.application.port.out.UserReadQuery;
import com.ats.user.application.query.UserListQuery;
import com.ats.user.application.response.UserResponse;
import com.ats.user.infrastructure.persistence.converter.UserPersistenceConverter;
import com.ats.user.infrastructure.persistence.mapper.UserMyBatisMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisUserReadQuery implements UserReadQuery {

    private final UserMyBatisMapper mapper;

    public MyBatisUserReadQuery(UserMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResponse<UserResponse> search(UserListQuery query) {
        String keyword = LikePatterns.containsLower(query.keyword());
        String role = query.role() == null ? null : query.role().name();
        String status = query.status() == null ? null : query.status().name();
        long total = mapper.countActive(keyword, role, status);
        List<UserResponse> items = total == 0 ? List.of()
                : mapper.searchActive(keyword, role, status, query.size(), (query.page() - 1L) * query.size())
                .stream()
                .map(UserPersistenceConverter::toAggregate)
                .map(UserResponse::from)
                .toList();
        return PageResponse.of(items, query.page(), query.size(), total);
    }
}
