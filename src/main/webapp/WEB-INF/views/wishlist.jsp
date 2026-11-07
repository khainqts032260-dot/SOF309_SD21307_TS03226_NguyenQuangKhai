<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Sản phẩm yêu thích | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="wishlist-hero fashion-container">
    <p class="subpage-kicker">My selection</p>
    <h1>Sản phẩm yêu thích</h1>
    <p>Lưu lại những thiết kế bạn quan tâm để xem lại và mua sắm thuận tiện hơn.</p>
</section>
<section class="fashion-section fashion-container wishlist-page" data-wishlist-page>
    <c:choose>
        <c:when test="${empty products}">
            <div class="wishlist-empty">
                <i class="fa-regular fa-heart"></i><h2>Danh sách yêu thích đang trống</h2>
                <p>Nhấn biểu tượng trái tim trên sản phẩm để lưu thiết kế bạn thích.</p>
                <a class="fashion-btn dark" href="${ctx}/products">Khám phá sản phẩm</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="fashion-product-grid office-product-grid wishlist-grid">
                <c:forEach var="p" items="${products}">
                    
                    <c:set var="cardFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
                    <article class="fashion-product-card office-product-card" data-wishlist-item="${p.maSP}">
                        <a class="fashion-product-media" href="${ctx}/product-detail?id=${p.maSP}">
                            <c:choose><c:when test="${not empty p.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${p.hinhAnh}" data-fallback="${cardFallback}" alt="${p.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${cardFallback}" alt="${p.tenSP}"></c:otherwise></c:choose>
                            <span class="quick-view">Xem sản phẩm</span>
                        </a>
                        <button class="product-card-heart active" type="button" data-wishlist-toggle data-product-id="${p.maSP}" data-remove-card="true" aria-pressed="true" aria-label="Bỏ khỏi sản phẩm yêu thích"><i class="fa-solid fa-heart"></i></button>
                        <div class="fashion-product-info">
                            <small>${p.tenDM}</small><h3><a href="${ctx}/product-detail?id=${p.maSP}">${p.tenSP}</a></h3>
                            <div class="fashion-price"><b><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ</b></div>
                            <form action="${ctx}/cart" method="post" data-ajax-cart><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="${p.maSP}"><input type="hidden" name="quantity" value="1"><button class="fashion-add-btn" type="submit">Thêm vào giỏ</button></form>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="common/footer.jsp" %>
