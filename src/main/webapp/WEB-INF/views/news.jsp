<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Tin tức | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="news-page refined-news-page" aria-label="Trang tin tức">
    <nav class="subpage-breadcrumb"><a href="${ctx}/home">Trang chủ</a><span>/</span><strong>Tin tức</strong></nav>
    <header class="editorial-page-title news-page-heading"><p class="subpage-kicker">Celine Closet updates</p><h1>TIN TỨC</h1><p>Bộ sưu tập mới, chương trình ưu đãi và những cập nhật từ cửa hàng.</p></header>
    <nav class="news-category-filter" aria-label="Lọc loại tin tức">
        <a class="${empty param.type ? 'active' : ''}" href="${ctx}/news">Tất cả</a>
        <c:forEach var="category" items="${newsCategories}"><a class="${param.type == category.maLoaiTin ? 'active' : ''}" href="${ctx}/news?type=${category.maLoaiTin}">${category.tenLoai}</a></c:forEach>
    </nav>
    <section class="public-news-grid">
        <c:forEach var="n" items="${newsList}">
            <article class="public-news-card">
                <a class="public-news-card-media" href="${ctx}/news-detail?id=${n.maTin}">
                    <c:choose><c:when test="${not empty n.hinhAnh}"><img src="${ctx}/${n.hinhAnh}" alt="${n.tieuDe}"></c:when><c:otherwise><img src="${ctx}/assets/images/fashion/hero-01.jpg" alt="${n.tieuDe}"></c:otherwise></c:choose>
                </a>
                <div><small><c:out value="${empty n.tenLoai ? 'Tin Celine Closet' : n.tenLoai}" /></small><h2><a href="${ctx}/news-detail?id=${n.maTin}"><c:out value="${n.tieuDe}" /></a></h2><p><c:out value="${n.tomTat}" /></p><a class="public-news-read-more" href="${ctx}/news-detail?id=${n.maTin}">Đọc bài viết <span>→</span></a></div>
            </article>
        </c:forEach>
        <c:if test="${empty newsList}"><div class="empty-box wide">Chưa có tin tức trong loại này.</div></c:if>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
