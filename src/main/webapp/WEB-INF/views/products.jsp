<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Sản phẩm | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>

<section class="fashion-page-banner shop-page-banner office-shop-banner">
    <img src="${ctx}/assets/images/fashion/hero-01.jpg" alt="Bộ sưu tập thời trang công sở Celine Closet">
    <div>
        <span>Office collection</span>
        <h1><c:choose><c:when test="${not empty selectedCategory}">${selectedCategory.tenDM}</c:when><c:otherwise>THỜI TRANG CÔNG SỞ</c:otherwise></c:choose></h1>
        <p>Phom dáng thanh lịch, bảng màu trung tính và những thiết kế dễ phối mỗi ngày.</p>
    </div>
</section>

<nav class="shop-category-tabs fashion-container" aria-label="Danh mục sản phẩm">
    <a href="${ctx}/products" class="${empty param.cat ? 'active' : ''}">Tất cả</a>
    <c:forEach var="c" items="${categories}">
        <a href="${ctx}/products?cat=${c.maDM}" class="${param.cat == c.maDM ? 'active' : ''}">${c.tenDM}</a>
    </c:forEach>
</nav>

<section class="fashion-section fashion-container shop-modern-layout office-shop-layout">
    <aside class="modern-filter-panel office-filter-panel" data-filter-panel>
        <div class="filter-heading">
            <div><small>Refine your selection</small><h2>Bộ lọc sản phẩm</h2></div>
            <a href="${ctx}/products">Xóa lọc</a>
        </div>
        <form action="${ctx}/products" method="get" class="complete-product-filter office-filter-form">
            <div class="filter-field filter-field-keyword">
                <label for="filterKeyword">Từ khóa</label>
                <div class="filter-search">
                    <input id="filterKeyword" name="q" value="${param.q}" placeholder="Tên, màu sắc, chất liệu...">
                    <button type="submit" aria-label="Tìm kiếm"><i class="fa-solid fa-magnifying-glass"></i></button>
                </div>
            </div>

            <div class="filter-field">
                <label for="filterCategory">Danh mục</label>
                <select id="filterCategory" name="cat">
                    <option value="">Tất cả sản phẩm</option>
                    <c:forEach var="c" items="${categories}">
                        <option value="${c.maDM}" ${param.cat == c.maDM ? 'selected' : ''}>${c.tenDM}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="filter-field">
                <label for="filterPrice">Khoảng giá</label>
                <select id="filterPrice" name="price">
                    <option value="" ${empty param.price ? 'selected' : ''}>Tất cả mức giá</option>
                    <option value="under300" ${param.price == 'under300' ? 'selected' : ''}>Dưới 300.000đ</option>
                    <option value="300to500" ${param.price == '300to500' ? 'selected' : ''}>300.000đ – 500.000đ</option>
                    <option value="500to800" ${param.price == '500to800' ? 'selected' : ''}>500.000đ – 800.000đ</option>
                    <option value="over800" ${param.price == 'over800' ? 'selected' : ''}>Trên 800.000đ</option>
                </select>
            </div>

            <fieldset class="size-filter-fieldset filter-field">
                <legend>Size sản phẩm</legend>
                <div class="real-size-list">
                    <label><input type="radio" name="size" value="" ${empty param.size ? 'checked' : ''}><span>Tất cả</span></label>
                    <label><input type="radio" name="size" value="S" ${param.size == 'S' ? 'checked' : ''}><span>S</span></label>
                    <label><input type="radio" name="size" value="M" ${param.size == 'M' ? 'checked' : ''}><span>M</span></label>
                    <label><input type="radio" name="size" value="L" ${param.size == 'L' ? 'checked' : ''}><span>L</span></label>
                    <label><input type="radio" name="size" value="XL" ${param.size == 'XL' ? 'checked' : ''}><span>XL</span></label>
                    <label><input type="radio" name="size" value="FREESIZE" ${param.size == 'FREESIZE' ? 'checked' : ''}><span>Free</span></label>
                </div>
            </fieldset>

            <div class="filter-field">
                <label for="filterSort">Sắp xếp</label>
                <select id="filterSort" name="sort">
                    <option value="" ${empty param.sort ? 'selected' : ''}>Mới nhất</option>
                    <option value="priceAsc" ${param.sort == 'priceAsc' ? 'selected' : ''}>Giá thấp đến cao</option>
                    <option value="priceDesc" ${param.sort == 'priceDesc' ? 'selected' : ''}>Giá cao đến thấp</option>
                    <option value="nameAsc" ${param.sort == 'nameAsc' ? 'selected' : ''}>Tên A – Z</option>
                </select>
            </div>

            <button class="fashion-btn dark filter-submit" type="submit">Áp dụng bộ lọc</button>
        </form>
    </aside>

    <div class="shop-results">
        <div class="shop-result-top office-result-top">
            <div>
                <button class="mobile-filter-button" type="button" data-filter-toggle><i class="fa-solid fa-sliders"></i> Bộ lọc</button>
                <span><b>${fn:length(products)}</b> thiết kế phù hợp</span>
            </div>
            <div class="active-filter-summary">
                <c:if test="${not empty param.price}"><span>Đã lọc giá</span></c:if>
                <c:if test="${not empty param.size}"><span>Size ${param.size}</span></c:if>
                <c:if test="${not empty param.q}"><span>“${param.q}”</span></c:if>
            </div>
        </div>

        <div class="fashion-product-grid shop-fashion-grid office-product-grid">
            <c:choose>
                <c:when test="${empty products}"><div class="empty-box wide">Chưa có sản phẩm phù hợp. Hãy thử bỏ bớt một điều kiện lọc.</div></c:when>
                <c:otherwise>
                    <c:forEach var="p" items="${products}" varStatus="st">
                        
                        <c:set var="cardFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
                        <article class="fashion-product-card office-product-card">
                            <a class="fashion-product-media" href="${ctx}/product-detail?id=${p.maSP}">
                                <span class="product-card-label">NEW ARRIVAL</span>
                                <c:choose>
                                    <c:when test="${not empty p.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${p.hinhAnh}" data-fallback="${cardFallback}" alt="${p.tenSP}"></c:when>
                                    <c:otherwise><img class="js-fashion-image" src="${cardFallback}" alt="${p.tenSP}"></c:otherwise>
                                </c:choose>
                                <span class="quick-view">Xem sản phẩm</span>
                            </a>
                            <button class="product-card-heart ${wishlistMap[p.maSP] ? 'active' : ''}" type="button" data-wishlist-toggle data-product-id="${p.maSP}" aria-pressed="${wishlistMap[p.maSP] ? 'true' : 'false'}" aria-label="${wishlistMap[p.maSP] ? 'Bỏ khỏi sản phẩm yêu thích' : 'Thêm vào sản phẩm yêu thích'}"><i class="${wishlistMap[p.maSP] ? 'fa-solid' : 'fa-regular'} fa-heart"></i></button>
                            <div class="fashion-product-info">
                                <small>${p.tenDM}</small>
                                <h3><a href="${ctx}/product-detail?id=${p.maSP}">${p.tenSP}</a></h3>
                                <div class="product-color-size"><span>${p.mauSac}</span><span>${p.kichThuoc}</span></div>
                                <div class="fashion-price"><b><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ</b></div>
                                <form action="${ctx}/cart" method="post" data-ajax-cart>
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="productId" value="${p.maSP}">
                                    <input type="hidden" name="quantity" value="1">
                                    <button class="fashion-add-btn" type="submit">Thêm nhanh vào giỏ</button>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<%@ include file="common/footer.jsp" %>
