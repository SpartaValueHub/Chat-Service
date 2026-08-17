package com.sparta.chat_service.adaptor.out.member;

import com.sparta.chat_service.adaptor.out.member.dto.MemberPublicProfileFeignResponse;
import com.sparta.chat_service.application.port.out.LoadMemberProfilePort;
import com.sparta.chat_service.domain.exception.MemberProfileUnavailableException;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Member 공개 프로필 Feign Adapter
@Component
@RequiredArgsConstructor
public class MemberProfileFeignAdapter implements LoadMemberProfilePort {

	// Member 서비스 Feign 클라이언트
	private final MemberFeignClient memberFeignClient;

	@Override
	public Optional<ChatUserProfile> findByMemberUuid(String memberUuid) {
		try {
			MemberPublicProfileFeignResponse response = memberFeignClient.getPublicProfile(memberUuid);
			return toDomain(memberUuid, response);
		} catch (FeignException.NotFound ignored) {
			return Optional.empty();
		} catch (FeignException exception) {
			throw new MemberProfileUnavailableException("회원 프로필을 조회할 수 없습니다.", exception);
		}
	}

	private Optional<ChatUserProfile> toDomain(String requestedMemberUuid, MemberPublicProfileFeignResponse response) {
		if (response == null || response.getNickname() == null || response.getNickname().isBlank()) {
			throw new MemberProfileUnavailableException("회원 프로필 응답이 올바르지 않습니다.");
		}
		String memberUuid = response.getMemberUuid() == null || response.getMemberUuid().isBlank()
				? requestedMemberUuid
				: response.getMemberUuid();
		return Optional.of(ChatUserProfile.create(
				memberUuid,
				response.getNickname(),
				response.getProfileImageUrl()
		));
	}
}
