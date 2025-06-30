package com.API.Documents_Management.Filters;

import com.API.Documents_Management.Dto.CourrielFilterRequest;
import com.API.Documents_Management.Entities.Courriel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CourrielSpecifications {
    public static Specification<Courriel> withFilters(CourrielFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.courrielNumber() != null)
                predicates.add(cb.like(cb.lower(root.get("courrielNumber")), "%" + filter.courrielNumber().toLowerCase() + "%"));
            if (filter.courrielType() != null)
                predicates.add(cb.equal(root.get("courrielType"), filter.courrielType()));
            if (filter.fromDivisionId() != null)
                predicates.add(cb.equal(root.get("fromDivision").get("id"), filter.fromDivisionId()));
            if (filter.toDivisionId() != null)
                predicates.add(cb.equal(root.get("toDivision").get("id"), filter.toDivisionId()));
            if (filter.fromDirectionId() != null)
                predicates.add(cb.equal(root.get("fromDirection").get("id"), filter.fromDirectionId()));
            if (filter.toDirectionId() != null)
                predicates.add(cb.equal(root.get("toDirection").get("id"), filter.toDirectionId()));
            if (filter.fromSousDirectionId() != null)
                predicates.add(cb.equal(root.get("fromSousDirection").get("id"), filter.fromSousDirectionId()));
            if (filter.toSousDirectionId() != null)
                predicates.add(cb.equal(root.get("toSousDirection").get("id"), filter.toSousDirectionId()));
            if (filter.fromExternalId() != null)
                predicates.add(cb.equal(root.get("fromExternal").get("id"), filter.fromExternalId()));
            if (filter.toExternalId() != null)
                predicates.add(cb.equal(root.get("toExternal").get("id"), filter.toExternalId()));


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
