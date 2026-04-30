package com.campus.campus_system.service;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.repository.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class HelpRequestService {
    @Autowired
    private HelpRequestRepository helpRequestRepository;

    // 发布求助
    @Transactional
    public HelpRequest publishHelpRequest(HelpRequest helpRequest) {
        return helpRequestRepository.save(helpRequest);
    }

    // 获取所有待帮助的请求
    public List<HelpRequest> getAllPendingRequests() {
        return helpRequestRepository.findByStatus("pending");
    }

    // 根据分类获取求助
    public List<HelpRequest> getRequestsByCategory(String category) {
        return helpRequestRepository.findByCategoryAndStatus(category, "pending");
    }

    public List<HelpRequest> queryRequests(String keyword, String category, String urgency, String sort) {
        Specification<HelpRequest> specification = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), "pending"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), pattern),
                        criteriaBuilder.like(root.get("description"), pattern)
                ));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category.trim()));
            }

            if (urgency != null && !urgency.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("urgency"), urgency.trim()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<HelpRequest> results = helpRequestRepository.findAll(specification, resolveHelpSort(sort));
        if ("urgencyDesc".equals(sort)) {
            results.sort(Comparator
                    .comparingInt((HelpRequest request) -> urgencyRank(request.getUrgency())).reversed()
                    .thenComparing(HelpRequest::getId, Comparator.reverseOrder()));
        }
        return results;
    }

    // 搜索求助
    public List<HelpRequest> searchRequests(String keyword) {
        return queryRequests(keyword, null, null, "latest");
    }

    // 获取求助详情
    public HelpRequest getRequestById(Long id) {
        HelpRequest request = helpRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("求助不存在"));
        // 增加浏览次数
        request.setViewCount(request.getViewCount() + 1);
        helpRequestRepository.save(request);
        return request;
    }

    // 接受帮助（更新帮助者ID和状态）
    @Transactional
    public HelpRequest acceptHelp(Long requestId, Long helperId) {
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("求助不存在"));
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该求助已被接受");
        }
        request.setHelperId(helperId);
        request.setStatus("helping");
        return helpRequestRepository.save(request);
    }

    // 完成帮助
    @Transactional
    public HelpRequest completeHelp(Long requestId) {
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("求助不存在"));
        request.setStatus("completed");
        return helpRequestRepository.save(request);
    }

    // 获取用户发布的求助
    public List<HelpRequest> getRequestsByRequester(Long requesterId) {
        return helpRequestRepository.findByRequesterId(requesterId);
    }

    // 获取用户帮助的求助
    public List<HelpRequest> getRequestsByHelper(Long helperId) {
        return helpRequestRepository.findByHelperId(helperId);
    }

    private Sort resolveHelpSort(String sort) {
        if ("viewsDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "id");
    }

    private int urgencyRank(String urgency) {
        if ("high".equals(urgency)) {
            return 3;
        }
        if ("medium".equals(urgency)) {
            return 2;
        }
        if ("low".equals(urgency)) {
            return 1;
        }
        return 0;
    }
}

