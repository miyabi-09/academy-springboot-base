    package com.spring.springbootapplication.service;

    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import com.spring.springbootapplication.entity.User;
    import com.spring.springbootapplication.repository.UserRepository;

    import java.io.IOException;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.Optional;

    @Service
    @Transactional
    public class UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }       

        public User login(String email, String password) {
            Optional<User> optionalUser = userRepository.findByEmail(normalizeEmail(email));
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // パスワードをエンコード済みと比較
                if (passwordEncoder.matches(password, user.getPassword())) {
                    return user;
                }
            }
            return null;
        }

        private String normalizeEmail(String email) {
            return email == null ? null : email.trim().toLowerCase();
        }

        
        public boolean existsByEmail(String email) {
            return userRepository.existsByEmail(normalizeEmail(email));
        }

        public Optional<User> findUserByEmail(String email) {
            return userRepository.findByEmail(normalizeEmail(email));
        }

        public User registerUser(User user) {
            user.setEmail(normalizeEmail(user.getEmail()));
            // パスワードをハッシュ化してから保存
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }

        @Transactional
        public void saveProfile(User formUser) {
        // DBの最新を取って “必要な項目だけ” 更新する（avatar_* は触らない）
        User u = userRepository.findById(formUser.getId())
                .orElseThrow(IllegalArgumentException::new);

        // 必要な項目だけ更新（フォームで編集可能なものに絞る）
        u.setIntroduction(formUser.getIntroduction());

        userRepository.save(u);
        }

            // ===== ここから 画像のDB保存/取得 =====

        // 画像を DB(bytea) に保存（文言なしの例外を投げる）
        public void updateAvatar(Long userId, MultipartFile file) throws IOException {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(); // メッセージ不要
            }

        String ct = file.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
            // まれに contentType が取れない環境向けの簡易フォールバック（任意）
            String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            if      (name.endsWith(".png"))  ct = "image/png";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) ct = "image/jpeg";
            else if (name.endsWith(".gif"))  ct = "image/gif";
            if (ct == null || !ct.startsWith("image/")) {
                throw new IllegalArgumentException();
            }
        }

            User u = userRepository.findById(userId)
                    .orElseThrow(IllegalArgumentException::new); // メッセージ不要

            u.setAvatarData(file.getBytes()); // bytea へ
            u.setAvatarMime(ct);              // MIME を保持
            u.setAvatarPath(null);  // ファイル方式を使わない場合は無効化（任意）

            userRepository.save(u);
        }

        // 画像を返す用の小さな入れ物
        public static record AvatarPayload(byte[] bytes, String contentType) {}

        // 画像を DB から取得
        public Optional<AvatarPayload> loadAvatar(Long userId) {
            return userRepository.findById(userId)
                    .filter(u -> u.getAvatarData() != null && u.getAvatarData().length > 0)
                    .map(u -> new AvatarPayload(u.getAvatarData(), u.getAvatarMime()));
        }


    }
