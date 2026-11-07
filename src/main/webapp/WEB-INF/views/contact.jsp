<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Liên hệ | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="contact-redesign-page">
    <nav class="subpage-breadcrumb"><a href="${ctx}/home">Trang chủ</a><span>/</span><a href="${ctx}/about">Giới thiệu</a><span>/</span><strong>Liên hệ</strong></nav>
    <section class="contact-redesign-hero">
        <div><p class="subpage-kicker">C&amp;C Care</p><h1>Chúng tôi luôn sẵn sàng lắng nghe.</h1><p>Liên hệ để được tư vấn sản phẩm, chọn size, đơn hàng hoặc chính sách thành viên.</p></div>
        <div class="contact-quick-list">
            <a href="tel:${shopHotline}"><i class="fa-solid fa-phone"></i><span>Hotline<strong>${shopHotline}</strong></span></a>
            <a href="mailto:${shopEmail}"><i class="fa-regular fa-envelope"></i><span>Email<strong>${shopEmail}</strong></span></a>
            <div><i class="fa-solid fa-location-dot"></i><span>Cửa hàng<strong>${shopAddress}</strong></span></div>
        </div>
    </section>
    <section class="contact-redesign-body">
        <form class="contact-form-clean" data-contact-form>
            <div><p class="subpage-kicker">Gửi lời nhắn</p><h2>C&amp;C có thể giúp gì cho bạn?</h2></div>
            <label>Họ và tên<input name="name" required placeholder="Nhập họ tên"></label>
            <label>Email<input type="email" name="email" required placeholder="name@email.com"></label>
            <label>Số điện thoại<input name="phone" placeholder="0xxxxxxxxx"></label>
            <label>Chủ đề<select name="topic"><option>Tư vấn sản phẩm</option><option>Đơn hàng và giao nhận</option><option>Đổi size / đổi trả</option><option>Thành viên và voucher</option><option>Khác</option></select></label>
            <label class="full">Nội dung<textarea name="message" rows="5" required placeholder="Nội dung cần hỗ trợ"></textarea></label>
            <button class="btn btn-dark" type="submit">Gửi liên hệ</button><small data-contact-message></small>
        </form>
        <aside class="contact-social-panel">
            <p class="subpage-kicker">Theo dõi C&amp;C</p><h2>Kết nối qua mạng xã hội</h2>
            <a href="${shopFacebook}" target="_blank" rel="noopener"><i class="fa-brands fa-facebook-f"></i><span>Facebook<small>Tin mới và hỗ trợ nhanh</small></span><b>→</b></a>
            <a href="${shopInstagram}" target="_blank" rel="noopener"><i class="fa-brands fa-instagram"></i><span>Instagram<small>Lookbook và cảm hứng phối đồ</small></span><b>→</b></a>
            <a href="${shopTiktok}" target="_blank" rel="noopener"><i class="fa-brands fa-tiktok"></i><span>TikTok<small>Video sản phẩm và cách phối</small></span><b>→</b></a>
            <a href="${shopYoutube}" target="_blank" rel="noopener"><i class="fa-brands fa-youtube"></i><span>YouTube<small>Câu chuyện bộ sưu tập</small></span><b>→</b></a>
        </aside>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
