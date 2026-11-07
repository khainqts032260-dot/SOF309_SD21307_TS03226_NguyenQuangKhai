<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Về chúng tôi | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="brand-subpage about-editorial-page">
    <nav class="subpage-breadcrumb" aria-label="Đường dẫn"><a href="${ctx}/home">Trang chủ</a><span>/</span><strong>Về chúng tôi</strong></nav>

    <section class="about-editorial-hero">
        <div class="about-editorial-hero-copy">
            <p class="subpage-kicker">Celine Closet · Modern Officewear</p>
            <h1>Thanh lịch để bạn<br>tự tin là chính mình.</h1>
            <p>Celine Closet theo đuổi những thiết kế nữ tính, gọn gàng và dễ ứng dụng — đủ chỉn chu cho công việc, đủ mềm mại cho những cuộc hẹn và đủ thoải mái để đồng hành cả ngày.</p>
            <div class="about-editorial-actions"><a class="fashion-btn dark" href="${ctx}/products">Khám phá bộ sưu tập</a><a class="about-text-link" href="${ctx}/brand-values">Giá trị thương hiệu <span>→</span></a></div>
        </div>
        <div class="about-editorial-hero-media">
            <img class="about-editorial-main-image" src="${ctx}/assets/images/fashion/hero-02.jpg" alt="Phong cách công sở thanh lịch Celine Closet">
            <img class="about-editorial-float-image" src="${ctx}/assets/images/fashion/card-06.jpg" alt="Chi tiết thiết kế Celine Closet">
            <span class="about-editorial-stamp">C&amp;C<br><small>Everyday elegance</small></span>
        </div>
    </section>

    <section class="about-editorial-stats" aria-label="Dịch vụ nổi bật">
        <article><strong>01</strong><div><b>Officewear focus</b><span>Phom dáng thanh lịch, dễ phối hằng ngày</span></div></article>
        <article><strong>02</strong><div><b>Tư vấn size</b><span>Hỗ trợ chọn phom theo số đo và sở thích</span></div></article>
        <article><strong>03</strong><div><b>Chăm sóc sau mua</b><span>Theo dõi đơn, đổi size và hỗ trợ nhanh</span></div></article>
        <article><strong>04</strong><div><b>C&amp;C Care</b><span>Chat AI kết hợp nhân viên tư vấn trực tiếp</span></div></article>
    </section>

    <section class="about-editorial-story">
        <div class="about-editorial-story-media">
            <img src="${ctx}/assets/images/fashion/story.jpg" alt="Câu chuyện Celine Closet">
            <div class="about-editorial-story-note"><span>OUR STORY</span><p>Một tủ đồ tốt không cần quá nhiều món — chỉ cần những món bạn thật sự muốn mặc lại.</p></div>
        </div>
        <article>
            <p class="subpage-kicker">Câu chuyện thương hiệu</p>
            <h2>Mặc đẹp theo cách nhẹ nhàng nhất.</h2>
            <p>C&amp;C bắt đầu từ mong muốn tạo ra những thiết kế có phom dáng chỉn chu nhưng không cứng nhắc. Chúng tôi tìm kiếm sự cân bằng giữa vẻ đẹp hiện đại, nét nữ tính tinh tế và cảm giác thoải mái khi mặc cả ngày.</p>
            <p>Từ bản phác thảo, lựa chọn chất liệu đến lần thử phom cuối cùng, mỗi sản phẩm đều được xem xét ở góc độ thực tế: dễ phối, phù hợp nhiều hoàn cảnh và giúp người mặc cảm thấy tự nhiên.</p>
            <a class="about-text-link" href="${ctx}/lookbook">Xem Lookbook C&amp;C <span>→</span></a>
        </article>
    </section>

    <section class="about-editorial-values">
        <header><p class="subpage-kicker">Điều chúng tôi theo đuổi</p><h2>Đẹp hơn không có nghĩa là phức tạp hơn.</h2></header>
        <div class="about-editorial-value-grid">
            <article><span>01</span><i class="fa-solid fa-pen-ruler"></i><h3>Thiết kế có chủ đích</h3><p>Mỗi đường cắt, chiếc cúc và tỷ lệ đều phục vụ phom dáng, độ thoải mái hoặc khả năng ứng dụng.</p></article>
            <article><span>02</span><i class="fa-solid fa-feather-pointed"></i><h3>Chất liệu dễ chịu</h3><p>Ưu tiên bề mặt mềm, độ rủ phù hợp và cách chăm sóc thuận tiện trong nhịp sống hằng ngày.</p></article>
            <article><span>03</span><i class="fa-regular fa-comments"></i><h3>Tư vấn rõ ràng</h3><p>Thông tin size, màu, chất liệu, cách phối và chính sách sau bán được trình bày minh bạch.</p></article>
            <article><span>04</span><i class="fa-solid fa-repeat"></i><h3>Mặc lại nhiều lần</h3><p>Chúng tôi hướng đến những món đồ dễ kết hợp và vẫn giữ vẻ thanh lịch qua nhiều mùa.</p></article>
        </div>
    </section>

    <section class="about-editorial-process">
        <div class="about-editorial-process-copy"><p class="subpage-kicker">From idea to wardrobe</p><h2>Một thiết kế đi qua nhiều lần cân nhắc trước khi đến tủ đồ của bạn.</h2></div>
        <div class="about-editorial-process-steps">
            <article><strong>01</strong><h3>Ý tưởng</h3><p>Xác định nhu cầu mặc, hoàn cảnh sử dụng và tinh thần của bộ sưu tập.</p></article>
            <article><strong>02</strong><h3>Chất liệu &amp; phom</h3><p>Chọn chất liệu, thử tỷ lệ và điều chỉnh để trang phục vừa đẹp vừa dễ vận động.</p></article>
            <article><strong>03</strong><h3>Hoàn thiện</h3><p>Kiểm tra chi tiết, màu sắc, khả năng phối và trải nghiệm thực tế khi mặc.</p></article>
            <article><strong>04</strong><h3>Đồng hành</h3><p>Tư vấn size, giao hàng, đánh giá và chăm sóc sau mua ngay trên Celine Closet.</p></article>
        </div>
    </section>

    <section class="about-editorial-gallery">
        <img src="${ctx}/assets/images/fashion/card-03.jpg" alt="Phong cách Celine Closet 1">
        <div><p class="subpage-kicker">C&amp;C wardrobe</p><h2>Cho những ngày bạn muốn trông thật chỉn chu mà vẫn là chính mình.</h2><a class="fashion-btn light" href="${ctx}/contact">Liên hệ Celine Closet</a></div>
        <img src="${ctx}/assets/images/fashion/card-05.jpg" alt="Phong cách Celine Closet 2">
    </section>

    <section class="about-editorial-cta">
        <div><p class="subpage-kicker">Find your next look</p><h2>Bắt đầu từ một thiết kế bạn yêu thích.</h2><p>Khám phá sản phẩm mới hoặc trò chuyện với C&amp;C Care để được gợi ý theo nhu cầu của bạn.</p></div>
        <div><a class="fashion-btn dark" href="${ctx}/products">Mua sắm ngay</a><a class="fashion-btn light" href="${ctx}/support">C&amp;C Care</a></div>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
