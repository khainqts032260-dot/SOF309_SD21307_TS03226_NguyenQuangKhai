<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Lookbook | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="lookbook-index-page expanded-lookbook-page">
    <nav class="subpage-breadcrumb"><a href="${ctx}/home">Trang chủ</a><span>/</span><strong>Lookbook</strong></nav>
    <header class="lookbook-index-header editorial-page-title"><p class="subpage-kicker">Office Lookbook 2026</p><h1>LOOKBOOK</h1><p>Tám câu chuyện phối đồ từ tháng 1 đến tháng 8, sử dụng trực tiếp các thiết kế đang có tại Celine Closet.</p></header>
    <section class="lookbook-expanded-grid">
        <a class="lookbook-story-card large" href="${ctx}/lookbook-detail?slug=month-01"><img src="${ctx}/assets/images/lookbook/lookbook-01.png" alt="Lookbook tháng 1"><div><small>01 · JANUARY</small><h2>Khởi đầu thanh lịch</h2><p>Gam beige, blazer và sơ mi cho một khởi đầu chỉn chu.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card tall" href="${ctx}/lookbook-detail?slug=month-02"><img src="${ctx}/assets/images/lookbook/lookbook-02.png" alt="Lookbook tháng 2"><div><small>02 · FEBRUARY</small><h2>Sắc xuân tinh tế</h2><p>Nữ tính vừa đủ với gile, áo hai dây và chân váy midi.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card" href="${ctx}/lookbook-detail?slug=month-03"><img src="${ctx}/assets/images/lookbook/lookbook-03.png" alt="Lookbook tháng 3"><div><small>03 · MARCH</small><h2>Nhịp điệu hiện đại</h2><p>Sơ mi cơ bản, denim và giày mũi nhọn cho ngày bận rộn.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card" href="${ctx}/lookbook-detail?slug=month-04"><img src="${ctx}/assets/images/lookbook/lookbook-04.png" alt="Lookbook tháng 4"><div><small>04 · APRIL</small><h2>Nét đẹp tự tin</h2><p>Đầm công sở, túi phom hộp và giày cao gót thanh lịch.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card wide" href="${ctx}/lookbook-detail?slug=month-05"><img src="${ctx}/assets/images/lookbook/lookbook-05.png" alt="Lookbook tháng 5"><div><small>05 · MAY</small><h2>Thanh lịch mùa hạ</h2><p>Chất liệu nhẹ, màu sáng và phụ kiện tinh gọn.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card" href="${ctx}/lookbook-detail?slug=month-06"><img src="${ctx}/assets/images/lookbook/lookbook-06.png" alt="Lookbook tháng 6"><div><small>06 · JUNE</small><h2>Gam màu trung tính</h2><p>Đen, trắng và xám phối theo tinh thần tối giản.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card" href="${ctx}/lookbook-detail?slug=month-07"><img src="${ctx}/assets/images/lookbook/lookbook-07.png" alt="Lookbook tháng 7"><div><small>07 · JULY</small><h2>Dáng vẻ đô thị</h2><p>Set sơ mi, quần jeans và phụ kiện cho nhịp sống thành thị.</p><span>Xem bộ phối →</span></div></a>
        <a class="lookbook-story-card wide" href="${ctx}/lookbook-detail?slug=month-08"><img src="${ctx}/assets/images/lookbook/lookbook-08.png" alt="Lookbook tháng 8"><div><small>08 · AUGUST</small><h2>Dấu ấn vượt thời gian</h2><p>Những thiết kế công sở dễ mặc qua nhiều mùa.</p><span>Xem bộ phối →</span></div></a>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
