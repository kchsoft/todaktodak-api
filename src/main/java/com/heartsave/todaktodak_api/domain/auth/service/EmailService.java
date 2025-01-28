package com.heartsave.todaktodak_api.domain.auth.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import com.heartsave.todaktodak_api.domain.auth.cache.OtpCache;
import com.heartsave.todaktodak_api.domain.auth.dto.request.EmailCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.EmailOtpCheckRequest;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final JavaMailSender mailSender;
  private final OtpCache cacheRepository;
  private final MemberRepository memberRepository;

  @Value("${spring.mail.username}")
  private String provider;

  public void sendOtp(EmailCheckRequest dto) {
    String email = dto.email();
    // 중복 확인
    if (memberRepository.existsByEmail(email))
      throw new AuthException(AuthErrorSpec.BASE_DUPLICATED_EMAIL);
    var otp = createOtp();
    var message = mailSender.createMimeMessage();
    try {
      var helper = new MimeMessageHelper(message, true, "UTF-8");
      setMessageContent(helper, email, otp);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new AuthException(AuthErrorSpec.EMAIL_OTP_SEND_FAIL);
    }
    cacheRepository.set(email, otp);
  }

  public boolean verifyOtp(EmailOtpCheckRequest dto) {
    String actualOtp = cacheRepository.get(dto.email());

    if (actualOtp != null && actualOtp.equals(dto.emailOtp())) {
      cacheRepository.delete(dto.email());
      return true;
    }
    logger.info("이메일 인증 실패 {}", dto.emailOtp());
    return false;
  }

  private String createOtp() {
    return RandomStringUtils.randomNumeric(6);
  }

  private void setMessageContent(MimeMessageHelper helper, String email, String otp)
      throws MessagingException {
    helper.setFrom(provider);
    helper.setTo(email);
    helper.setSubject("토닥토닥 이메일 인증번호");
    helper.setSentDate(new Date());
    helper.setText(
        String.format(
            """
        <!DOCTYPE html>
        <html>
        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #FAF7F0;">
                <div style="text-align: center; padding: 20px; background-color: #ffffff; border-radius: 8px; margin-bottom: 20px;">
                    <h1 style="color: #4A4947; margin: 0;">인증번호 안내</h1>
                </div>
                <div style="background-color: #D8D2C2; padding: 30px; border-radius: 8px; text-align: center;">
                    <p style="margin: 0 0 20px 0;">요청하신 인증번호를 안내드립니다.</p>

                    <div style="background-color: #FAF7F0; padding: 20px; border-radius: 6px; margin: 20px 0;">
                        <span style="font-size: 32px; letter-spacing: 5px; font-weight: bold; color: #000000;">%s</span>
                    </div>

                    <p style="margin: 0 0 10px 0;">인증번호는 <strong>3분</strong> 동안 유효합니다.</p>
                    <p style="margin: 0 0 10px 0;">본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.</p>
                </div>

                <div style="text-align: center; color: #4A4947; font-size: 12px; margin-top: 20px;">
                    <p style="margin: 0 0 5px 0;">본 메일은 발신전용이며 회신되지 않습니다.</p>
                </div>
            </div>
        </body>
        </html>
            """,
            otp),
        true);
  }
}
