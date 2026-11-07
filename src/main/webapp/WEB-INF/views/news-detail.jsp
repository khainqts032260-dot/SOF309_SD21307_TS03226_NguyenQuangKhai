<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${news.tieuDe} | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="news-detail-page">
    <nav class="subpage-breadcrumb" aria-label="Đường dẫn">
        <a href="${ctx}/home">Trang chủ</a><span>/</span><a href="${ctx}/news">Tin tức</a><span>/</span><strong><c:out value="${news.tieuDe}" /></strong>
    </nav>

    <article class="news-detail-article">
        <header class="news-detail-header">
            <p class="subpage-kicker"><c:out value="${empty news.tenLoai ? 'Celine Closet Journal' : news.tenLoai}" /></p>
            <h1><c:out value="${news.tieuDe}" /></h1>
            <p class="news-detail-summary"><c:out value="${news.tomTat}" /></p>
            <div class="news-detail-meta"><i class="fa-regular fa-calendar"></i><span>Cập nhật <fmt:formatDate value="${news.ngayCapNhat}" pattern="dd/MM/yyyy" /></span></div>
        </header>

        <figure class="news-detail-cover">
            <c:choose>
                <c:when test="${not empty news.hinhAnh}"><img src="${ctx}/${news.hinhAnh}" alt="${news.tieuDe}"></c:when>
                <c:otherwise><img src="${ctx}/assets/images/fashion/hero-01.jpg" alt="${news.tieuDe}"></c:otherwise>
            </c:choose>
        </figure>

        <div class="news-detail-content"><c:out value="${news.noiDung}" /></div>
        <footer class="news-detail-footer">
            <a class="fashion-btn light" href="${ctx}/news"><i class="fa-solid fa-arrow-left"></i> Quay lại tin tức</a>
            <a class="fashion-btn dark" href="${ctx}/products">Khám phá sản phẩm</a>
        </footer>
    </article>

    <c:set var="relatedCount" value="0" />
    <section class="news-related-section">
        <div class="news-related-heading"><div><p class="subpage-kicker">Có thể bạn quan tâm</p><h2>Tin cùng chuyên mục</h2></div><a href="${ctx}/news">Xem tất cả →</a></div>
        <div class="news-related-grid">
            <c:forEach var="item" items="${relatedNews}">
                <c:if test="${item.maTin != news.maTin && relatedCount < 3}">
                    <a class="news-related-card" href="${ctx}/news-detail?id=${item.maTin}">
                        <c:choose><c:when test="${not empty item.hinhAnh}"><img src="${ctx}/${item.hinhAnh}" alt="${item.tieuDe}"></c:when><c:otherwise><img src="${ctx}/assets/images/fashion/card-05.jpg" alt="${item.tieuDe}"></c:otherwise></c:choose>
                        <span><small><c:out value="${empty item.tenLoai ? 'Tin Celine Closet' : item.tenLoai}" /></small><strong><c:out value="${item.tieuDe}" /></strong><em>Đọc bài →</em></span>
                    </a>
                    <c:set var="relatedCount" value="${relatedCount + 1}" />
                </c:if>
            </c:forEach>
        </div>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
