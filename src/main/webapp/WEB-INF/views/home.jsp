<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Celine Closet | Thời trang công sở thanh lịch" scope="request" />
<%@ include file="common/header.jsp" %>

<main class="fashion-home refined-home office-luxe-home">
    <section class="sale-hero" data-sale-slider>
        <div class="sale-slides">
            <article class="sale-slide active" data-sale-slide="0">
                <img loading="eager" src="${ctx}/assets/images/fashion/hero-01.jpg" alt="Bộ sưu tập Work Chic của Celine Closet">
                <div class="sale-slide-overlay luxe-soft"></div>
                <div class="sale-slide-content dark-copy">
                    <p>New collection</p>
                    <h1>WORK CHIC<br>TIMELESS ELEGANCE</h1>
                    <span>Thanh lịch, tinh tế và dễ ứng dụng cho nhịp sống công sở hiện đại.</span>
                    <div><a class="fashion-btn dark" href="${ctx}/products">Mua ngay</a><a class="fashion-link dark" href="${ctx}/lookbook">Xem bộ sưu tập</a></div>
                </div>
            </article>
            <article class="sale-slide" data-sale-slide="1">
                <img loading="eager" decoding="async" src="${ctx}/assets/images/fashion/hero-03.jpg" alt="Chương trình ưu đãi giữa mùa">
                <div class="sale-slide-overlay"></div>
                <div class="sale-slide-content">
                    <p>Mid-season sale</p>
                    <h2>ƯU ĐÃI<br>ĐẾN 50%</h2>
                    <span>Ưu đãi giới hạn cho những thiết kế được yêu thích nhất trong mùa.</span>
                    <div><a class="fashion-btn light" href="${ctx}/products">Mua ngay</a><a class="fashion-link light" href="${ctx}/products">Xem sản phẩm sale</a></div>
                </div>
            </article>
            <article class="sale-slide" data-sale-slide="2">
                <img loading="eager" decoding="async" src="${ctx}/assets/images/fashion/hero-02.jpg" alt="Thiết kế công sở được hoàn thiện chỉn chu">
                <div class="sale-slide-overlay luxe-soft"></div>
                <div class="sale-slide-content dark-copy">
                    <p>Office essentials</p>
                    <h2>TỦ ĐỒ<br>TINH GỌN</h2>
                    <span>Blazer, áo kiểu, chân váy và set phối được chọn để mặc đẹp suốt ngày dài.</span>
                    <div><a class="fashion-btn dark" href="${ctx}/products?cat=4">Khám phá ngay</a></div>
                </div>
            </article>
            <article class="sale-slide" data-sale-slide="3">
                <img loading="eager" decoding="async" src="${ctx}/assets/images/fashion/hero-04.jpg" alt="Quà tặng dành cho thành viên Celine Closet">
                <div class="sale-slide-overlay"></div>
                <div class="sale-slide-content">
                    <p>Member week</p>
                    <h2>THÊM VÀO GIỎ<br>NHẬN THÊM ƯU ĐÃI</h2>
                    <span>Đăng nhập thành viên để tích điểm và nhận voucher dành riêng cho bạn.</span>
                    <div><a class="fashion-btn light" href="${ctx}/register">Đăng ký thành viên</a></div>
                </div>
            </article>
        </div>
        <button class="sale-arrow prev" type="button" data-sale-prev aria-label="Banner trước">‹</button>
        <button class="sale-arrow next" type="button" data-sale-next aria-label="Banner tiếp theo">›</button>
    </section>

    <section class="home-benefit-strip" aria-label="Chính sách mua hàng">
        <article><svg class="benefit-icon" viewBox="0 0 32 32" aria-hidden="true"><path d="M3 8h17v14H3z"/><path d="M20 13h5l4 4v5h-9z"/><circle cx="9" cy="24" r="3"/><circle cx="24" cy="24" r="3"/></svg><div><strong>Miễn phí giao hàng</strong><span>Đơn từ 699.000đ</span></div></article>
        <article><svg class="benefit-icon" viewBox="0 0 32 32" aria-hidden="true"><circle cx="16" cy="13" r="8"/><path d="m11 20-2 9 7-4 7 4-2-9"/><path d="m13 13 2 2 4-5"/></svg><div><strong>Sản phẩm chính hãng</strong><span>Cam kết đúng mô tả</span></div></article>
        <article><svg class="benefit-icon" viewBox="0 0 32 32" aria-hidden="true"><path d="M6 10 16 5l10 5-10 5z"/><path d="M6 10v12l10 5 10-5V10"/><path d="M9 19a8 8 0 0 0 13 4"/><path d="m9 19-4 1 2 4"/></svg><div><strong>Đổi trả dễ dàng</strong><span>Trong 7 ngày</span></div></article>
        <article><svg class="benefit-icon" viewBox="0 0 32 32" aria-hidden="true"><path d="M6 18v-3a10 10 0 0 1 20 0v3"/><path d="M6 18H3v7h5v-7H6Zm20 0h3v7h-5v-7h2Z"/><path d="M24 25c0 3-3 4-7 4"/></svg><div><strong>Hỗ trợ 24/7</strong><span>Hotline: 1900 1234</span></div></article>
    </section>

    <section class="fashion-section fashion-container compact-section home-promo-section">
        <div class="home-promo-grid">
            <a class="home-promo-card home-promo-sale" href="${ctx}/products">
                <img src="${ctx}/assets/images/fashion/card-07.jpg" alt="Ưu đãi giữa mùa đến 50 phần trăm">
                <div><small>Mid-season sale</small><h2>ƯU ĐÃI ĐẾN<br><b>50%</b></h2><span>Mua ngay →</span></div>
            </a>
            <c:forEach var="cat" items="${categories}" varStatus="st" end="2">
                <a class="home-promo-card" href="${ctx}/products?cat=${cat.maDM}">
                    <img src="${ctx}/assets/images/fashion/card-${st.index + 1 < 10 ? '0' : ''}${st.index + 1}.jpg" alt="${cat.tenDM}">
                    <div><small>${st.index == 0 ? 'New arrivals' : (st.index == 1 ? 'Best sellers' : 'Office style')}</small><h3>${cat.tenDM}</h3><span>Khám phá ngay →</span></div>
                </a>
            </c:forEach>
        </div>
    </section>

    <section class="fashion-section fashion-container compact-section" id="featured">
        <div class="fashion-heading compact-heading editorial-section-heading">
            <div><span>New arrivals</span><h2>SẢN PHẨM NỔI BẬT</h2><p class="reload-product-note">Những thiết kế công sở được yêu thích trong tuần</p></div>
            <a href="${ctx}/products">XEM TẤT CẢ</a>
        </div>
        <div class="fashion-product-grid compact-product-grid">
            <c:forEach var="p" items="${featuredProducts}" varStatus="st" end="11">
                
                <c:set var="homeCardFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
                <article class="fashion-product-card compact-product-card">
                    <a class="fashion-product-media" href="${ctx}/product-detail?id=${p.maSP}">
                        <span class="product-card-label">MỚI VỀ</span>
                        <c:choose>
                            <c:when test="${not empty p.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${p.hinhAnh}" data-fallback="${homeCardFallback}" alt="${p.tenSP}"></c:when>
                            <c:otherwise><img class="js-fashion-image" src="${homeCardFallback}" alt="${p.tenSP}"></c:otherwise>
                        </c:choose>
                        <span class="quick-view">Xem sản phẩm</span>
                    </a>
                    <button class="product-card-heart ${wishlistMap[p.maSP] ? 'active' : ''}" type="button" data-wishlist-toggle data-product-id="${p.maSP}" aria-pressed="${wishlistMap[p.maSP] ? 'true' : 'false'}" aria-label="${wishlistMap[p.maSP] ? 'Bỏ khỏi sản phẩm yêu thích' : 'Thêm vào sản phẩm yêu thích'}"><i class="${wishlistMap[p.maSP] ? 'fa-solid' : 'fa-regular'} fa-heart"></i></button>
                    <div class="fashion-product-info">
                        <small>${p.tenDM}</small>
                        <h3><a href="${ctx}/product-detail?id=${p.maSP}">${p.tenSP}</a></h3>
                        <div class="fashion-price"><b><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ</b></div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>

    <section class="editorial-sale-banner compact-story-banner">
        <img src="${ctx}/assets/images/fashion/story.jpg" alt="Câu chuyện thiết kế Celine Closet">
        <div class="editorial-sale-copy">
            <span>Celine Closet Studio</span>
            <h2>ĐẸP TỪ<br>PHOM DÁNG</h2>
            <p>Mỗi thiết kế được cân chỉnh để thanh lịch khi mặc và thoải mái khi chuyển động.</p>
            <a class="fashion-btn dark" href="${ctx}/about">Câu chuyện thương hiệu</a>
        </div>
    </section>

    <section class="fashion-section fashion-container collection-carousel-section">
        <div class="fashion-heading compact-heading editorial-section-heading">
            <div><span>Office edit</span><h2>BỘ PHỐI CÔNG SỞ</h2></div>
            <a href="${ctx}/lookbook">XEM TẤT CẢ</a>
        </div>
        <div class="editorial-carousel" data-carousel="lookbookHome">
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-01"><img src="${ctx}/assets/images/lookbook/lookbook-01.png" alt="Lookbook tháng 1"><div><small>01</small><h3>Khởi đầu thanh lịch</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-02"><img src="${ctx}/assets/images/lookbook/lookbook-02.png" alt="Lookbook tháng 2"><div><small>02</small><h3>Sắc xuân tinh tế</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-03"><img src="${ctx}/assets/images/lookbook/lookbook-03.png" alt="Lookbook tháng 3"><div><small>03</small><h3>Nhịp điệu hiện đại</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-04"><img src="${ctx}/assets/images/lookbook/lookbook-04.png" alt="Lookbook tháng 4"><div><small>04</small><h3>Nét đẹp tự tin</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-05"><img src="${ctx}/assets/images/lookbook/lookbook-05.png" alt="Lookbook tháng 5"><div><small>05</small><h3>Thanh lịch mùa hạ</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-06"><img src="${ctx}/assets/images/lookbook/lookbook-06.png" alt="Lookbook tháng 6"><div><small>06</small><h3>Gam màu trung tính</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-07"><img src="${ctx}/assets/images/lookbook/lookbook-07.png" alt="Lookbook tháng 7"><div><small>07</small><h3>Dáng vẻ đô thị</h3><span>Xem bộ phối</span></div></a>
            <a class="collection-tile" href="${ctx}/lookbook-detail?slug=month-08"><img src="${ctx}/assets/images/lookbook/lookbook-08.png" alt="Lookbook tháng 8"><div><small>08</small><h3>Dấu ấn vượt thời gian</h3><span>Xem bộ phối</span></div></a>
        </div>
    </section>

    <section class="fashion-review-section" id="feedback">
        <div class="fashion-container">
            <div class="fashion-heading light-heading compact-heading"><div><span>Customer reviews</span><h2>Khách hàng nói gì về Celine Closet?</h2></div></div>
            <div class="fashion-review-grid refined-review-grid">
                <c:forEach var="f" items="${feedbacks}" end="3">
                    <article class="home-review-card">
                        <c:if test="${not empty f.hinhAnh}"><img class="home-review-photo" src="${ctx}/${f.hinhAnh}" alt="Ảnh đánh giá của ${f.hoTen}"></c:if>
                        <div class="review-stars"><c:forEach begin="1" end="${f.soSao}">★</c:forEach></div>
                        <p>“${f.noiDung}”</p>
                        <strong>${f.hoTen}</strong>
                        <span>${f.daMuaHang == 1 ? 'Đã mua hàng xác thực' : 'Khách hàng Celine Closet'}</span>
                    </article>
                </c:forEach>
            </div>
            <div class="fashion-feedback-form refined-feedback-form">
                <div class="feedback-form-title"><span>Verified purchase</span><h3>Đánh giá sau khi mua hàng</h3></div>
                <p>Để bảo đảm đánh giá là trải nghiệm thật, Celine Closet chỉ cho phép đánh giá sản phẩm thuộc đơn hàng đã <b>Hoàn thành</b>.</p>
                <c:choose>
                    <c:when test="${not empty sessionScope.auth}">
                        <a class="fashion-btn light" href="${ctx}/orders">Xem sản phẩm đã mua</a>
                    </c:when>
                    <c:otherwise>
                        <a class="fashion-btn light" href="${ctx}/login">Đăng nhập để xem đơn hàng</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>
</main>

<%@ include file="common/footer.jsp" %>
