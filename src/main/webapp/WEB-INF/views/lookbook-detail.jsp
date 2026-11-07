<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${collectionTitle} | Lookbook Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="lookbook-detail-page">
    <nav class="subpage-breadcrumb"><a href="${ctx}/home">Trang chủ</a><span>/</span><a href="${ctx}/lookbook">Lookbook</a><span>/</span><strong>${collectionTitle}</strong></nav>
    <header class="lookbook-detail-header"><p class="subpage-kicker">Lookbook · Tháng ${collectionMonth}/2026</p><h1>${collectionTitle}</h1><p>${collectionLead}</p></header>
    <section class="lookbook-gallery supplied-lookbook-gallery">
        <figure class="gallery-wide lookbook-month-hero"><img src="${ctx}/assets/images/${heroImage}" alt="${collectionTitle}"></figure>
        <c:forEach var="p" items="${lookbookProducts}" varStatus="st">
            <figure class="${st.index == 1 ? 'gallery-tall' : (st.index == 3 ? 'gallery-wide' : '')}">
                <a href="${ctx}/product-detail?id=${p.maSP}"><img src="${ctx}/${p.hinhAnh}" alt="${p.tenSP}"></a>
                <figcaption><small>${p.tenDM}</small><strong>${p.tenSP}</strong><span><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ · Xem sản phẩm →</span></figcaption>
            </figure>
        </c:forEach>
    </section>
    <section class="lookbook-detail-story"><p class="subpage-kicker">Bộ phối trong tháng</p><h2>${collectionLead}</h2><p>Các sản phẩm trong bộ ảnh đều là sản phẩm đang có tại cửa hàng. Bạn có thể mở từng thiết kế để xem đầy đủ màu sắc, size, chất liệu và những góc ảnh thực tế.</p><a href="${ctx}/products">Khám phá toàn bộ sản phẩm →</a></section>
    <nav class="article-back-nav"><a href="${ctx}/lookbook">← Quay lại Lookbook</a><a href="${ctx}/news">Tin tức mới từ Celine Closet →</a></nav>
</main>
<%@ include file="common/footer.jsp" %>
