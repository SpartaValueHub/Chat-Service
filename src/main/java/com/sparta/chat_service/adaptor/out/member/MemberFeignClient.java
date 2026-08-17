package com.sparta.chat_service.adaptor.out.member;

import com.sparta.chat_service.adaptor.out.member.dto.MemberPublicProfileFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Member 서비스 공개 프로필 조회
@FeignClient(name = "member-service")
public interface MemberFeignClient {

	@GetMapping("/api/v1/members/{memberUuid}/profile")
	MemberPublicProfileFeignResponse getPublicProfile(@PathVariable("memberUuid") String memberUuid);
}
