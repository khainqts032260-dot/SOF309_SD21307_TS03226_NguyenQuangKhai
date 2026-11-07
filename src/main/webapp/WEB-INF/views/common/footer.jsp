<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<section class="fashion-service-strip">
    <article><b>01</b><div><strong>Miễn phí vận chuyển</strong><span>Đơn hàng từ 699.000đ</span></div></article>
    <article><b>02</b><div><strong>Đổi size trong 7 ngày</strong><span>Sản phẩm còn nguyên tem</span></div></article>
    <article><b>03</b><div><strong>Tích điểm thành viên</strong><span>Đổi voucher và quà tặng</span></div></article>
    <article><b>04</b><div><strong>Hỗ trợ nhanh</strong><span>Hotline ${shopHotline}</span></div></article>
</section>
<footer class="kk-footer" id="contact">
    <div class="footer-contact-block">
        <a class="footer-logo office-footer-logo" href="${ctx}/home">CELINE CLOSET<small>OFFICEWEAR</small></a>
        <p class="footer-tagline">Thời trang thanh lịch, dễ ứng dụng và gần gũi với nhịp sống mỗi ngày.</p>
        <a class="footer-hotline" href="tel:${shopHotline}"><i class="fa-solid fa-phone"></i>${shopHotline}</a>
        <a class="footer-email" href="mailto:${shopEmail}"><i class="fa-regular fa-envelope"></i>${shopEmail}</a>
        <div class="footer-socials labeled-socials" aria-label="Mạng xã hội Celine Closet">
            <a href="${shopFacebook}" target="_blank" rel="noopener" aria-label="Facebook"><i class="fa-brands fa-facebook-f"></i></a>
            <a href="${shopInstagram}" target="_blank" rel="noopener" aria-label="Instagram"><i class="fa-brands fa-instagram"></i></a>
            <a href="${shopTiktok}" target="_blank" rel="noopener" aria-label="TikTok"><i class="fa-brands fa-tiktok"></i></a>
            <a href="${shopYoutube}" target="_blank" rel="noopener" aria-label="YouTube"><i class="fa-brands fa-youtube"></i></a>
        </div>
    </div>
    <div class="footer-column">
        <h4>Khám phá</h4>
        <a href="${ctx}/about">Về chúng tôi</a>
        <a href="${ctx}/brand-values">Giá trị thương hiệu</a>
        <a href="${ctx}/lookbook">Lookbook</a>
        <a href="${ctx}/news">Tin tức</a>
    </div>
    <div class="footer-column">
        <h4>Hỗ trợ</h4>
        <a href="${ctx}/cart">Giỏ hàng</a>
        <a href="${ctx}/wishlist">Sản phẩm yêu thích</a>
        <a href="${ctx}/orders">Theo dõi đơn hàng</a>
        <a href="${ctx}/support">Hỗ trợ khách hàng</a>
        <a href="${ctx}/forgot-password">Khôi phục mật khẩu</a>
    </div>
    <div class="footer-column footer-newsletter">
        <h4>Nhận ưu đãi mới</h4>
        <p>Nhận mã giảm giá và thông tin bộ sưu tập mới từ Celine Closet.</p>
        <form data-newsletter><input type="email" placeholder="Email của bạn" required><button type="submit" aria-label="Đăng ký nhận tin"><i class="fa-regular fa-paper-plane"></i></button></form>
        <small data-newsletter-message></small>
        <a class="footer-address" href="${ctx}/contact"><i class="fa-solid fa-location-dot"></i>${shopAddress}</a>
    </div>
    <div class="footer-bottom"><span>© 2026 Celine Closet</span><span>Thanh lịch · Ứng dụng · Gần gũi</span></div>
</footer>
<div class="celine-floating-chat" id="celine-floating-chat" data-api-url="${chatApiUrl}">
    <button class="celine-chat-launcher" type="button" id="celine-chat-launcher" aria-label="Chat với shop" aria-expanded="false">
        <span class="celine-chat-launcher-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M21 12a8 8 0 0 1-8 8H6l-4 3 1.4-5A8 8 0 1 1 21 12Z"/><circle cx="8" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="16" cy="12" r="1"/></svg></span>
        <span class="celine-chat-launcher-label">Chat với shop</span>
        <span class="celine-chat-online-dot"></span>
    </button>
    <div class="celine-chat-frame-wrap" id="celine-chat-frame-wrap" aria-hidden="true">
        <iframe src="${ctx}/support?widget=1" title="Chat trực tiếp với Celine Closet" loading="lazy"></iframe>
    </div>
</div>
<script>
(function(){
 const launcher=document.getElementById('celine-chat-launcher'),wrap=document.getElementById('celine-chat-frame-wrap');
 if(!launcher||!wrap)return;
 function setOpen(open){wrap.classList.toggle('open',open);wrap.setAttribute('aria-hidden',String(!open));launcher.setAttribute('aria-expanded',String(open));launcher.classList.toggle('open',open);}
 launcher.addEventListener('click',()=>setOpen(!wrap.classList.contains('open')));
 window.addEventListener('message',e=>{if(e.data==='closeCelineChatbox')setOpen(false);});
})();
</script>
<c:if test="${needsMap}"><script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script></c:if>
<script src="${ctx}/assets/js/main.js?v=cart-form-action-fix-20260808-1756"></script>
</body>
</html>
