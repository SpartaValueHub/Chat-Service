package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ResolveChatUserProfileUseCase;
import com.sparta.chat_service.application.port.out.LoadChatUserProfilePort;
import com.sparta.chat_service.application.port.out.LoadMemberProfilePort;
import com.sparta.chat_service.application.port.out.SaveChatUserProfilePort;
import com.sparta.chat_service.domain.exception.ChatUserProfileNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidMemberUuidException;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolveChatUserProfileService implements ResolveChatUserProfileUseCase {

	// 로컬 Read Model 조회
	private final LoadChatUserProfilePort loadChatUserProfilePort;
	// 로컬 Read Model 저장
	private final SaveChatUserProfilePort saveChatUserProfilePort;
	// Member 서비스 공개 프로필 조회
	private final LoadMemberProfilePort loadMemberProfilePort;

	@Override
	@Transactional
	public ChatUserProfile resolve(String memberUuid) {
		String normalizedMemberUuid = memberUuid == null ? "" : memberUuid.trim();
		if (normalizedMemberUuid.isBlank()) {
			throw new InvalidMemberUuidException();
		}

		return loadChatUserProfilePort.findByMemberUuid(normalizedMemberUuid)
				.orElseGet(() -> fetchAndSave(normalizedMemberUuid));
	}

	private ChatUserProfile fetchAndSave(String memberUuid) {
		ChatUserProfile fetched = loadMemberProfilePort.findByMemberUuid(memberUuid)
				.orElseThrow(() -> new ChatUserProfileNotFoundException(memberUuid));
		return saveChatUserProfilePort.save(fetched);
	}
}
