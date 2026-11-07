<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${empty pageTitle ? 'Celine Closet' : pageTitle}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Celine Closet - Thời trang công sở nữ thanh lịch, tối giản và dễ ứng dụng.">
    <link rel="preconnect" href="https://cdnjs.cloudflare.com" crossorigin>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" referrerpolicy="no-referrer">
    <link rel="stylesheet" href="${ctx}/assets/css/styles.css?v=ux-fixes-20260808">
    <c:if test="${needsMap}"><link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""></c:if>
</head>
<body data-context-path="${ctx}" data-login-url="${ctx}/login?next=wishlist">
<div class="utility-bar luxe-utility-bar">
    <div class="promo-marquee" aria-label="Chương trình ưu đãi">
        <div class="promo-marquee-track">
            <span><i class="fa-solid fa-truck"></i> Miễn phí vận chuyển toàn quốc cho đơn từ 699.000đ</span>
            <span><i class="fa-solid fa-tag"></i> Mid-season sale: giảm đến 50% cho sản phẩm chọn lọc</span>
            <span><i class="fa-solid fa-rotate"></i> Đổi sản phẩm dễ dàng trong 7 ngày</span>
            <span><i class="fa-solid fa-gift"></i> Tặng voucher 100K cho thành viên mới</span>
            <span aria-hidden="true"><i class="fa-solid fa-truck"></i> Miễn phí vận chuyển toàn quốc cho đơn từ 699.000đ</span>
            <span aria-hidden="true"><i class="fa-solid fa-tag"></i> Mid-season sale: giảm đến 50% cho sản phẩm chọn lọc</span>
            <span aria-hidden="true"><i class="fa-solid fa-rotate"></i> Đổi sản phẩm dễ dàng trong 7 ngày</span>
            <span aria-hidden="true"><i class="fa-solid fa-gift"></i> Tặng voucher 100K cho thành viên mới</span>
        </div>
    </div>
    <div class="promo-countdown">Kết thúc sau: <strong data-sale-countdown>02 : 15 : 42 : 18</strong></div>
</div>

<header class="kk-header office-luxe-header">
    <button class="mobile-menu-toggle kk-menu-button" type="button" data-menu-toggle aria-label="Mở menu" aria-expanded="false">☰</button>

    <nav class="kk-nav kk-nav-left" data-primary-nav aria-label="Danh mục chính">
        <div class="nav-item has-dropdown ${activePath == '/about' || activePath == '/brand-values' || activePath == '/contact' || activePath == '/support' ? 'active' : ''}">
            <a href="${ctx}/about">Giới thiệu</a>
            <div class="nav-dropdown compact-dropdown about-dropdown">
                <p class="dropdown-kicker">Celine Closet</p>
                <a href="${ctx}/about"><span>01</span> Về chúng tôi</a>
                <a href="${ctx}/brand-values"><span>02</span> Giá trị thương hiệu</a>
                <a href="${ctx}/contact"><span>04</span> Liên hệ</a>
                <a href="${ctx}/support"><span>05</span> Hỗ trợ khách hàng</a>
            </div>
        </div>
        <div class="nav-item has-dropdown ${activePath == '/products' || activePath == '/product-detail' ? 'active' : ''}">
            <a href="${ctx}/products">Shop online</a>
            <div class="nav-dropdown shop-dropdown">
                <div>
                    <p class="dropdown-kicker">Danh mục</p>
                    <a href="${ctx}/products">Tất cả sản phẩm</a>
                    <c:forEach var="headerCategory" items="${headerCategories}" end="7">
                        <a href="${ctx}/products?cat=${headerCategory.maDM}">${headerCategory.tenDM}</a>
                    </c:forEach>
                </div>
                <div>
                    <p class="dropdown-kicker">Khám phá</p>
                    <a href="${ctx}/products">Hàng mới</a>
                    <a href="${ctx}/lookbook">Lookbook công sở</a>
                    <a href="${ctx}/news">Tin tức &amp; ưu đãi</a>
                    <a href="${ctx}/products?sort=priceAsc">Gợi ý dễ mua</a>
                </div>
                <a class="dropdown-image" href="${ctx}/products?cat=1">
                    <img src="${ctx}/assets/images/fashion/card-05.jpg" alt="Thời trang công sở Celine Closet">
                    <span>Office edit <b>Khám phá →</b></span>
                </a>
            </div>
        </div>
        <a class="${activePath == '/lookbook' || activePath == '/lookbook-detail' ? 'active' : ''}" href="${ctx}/lookbook">Lookbook</a>
        <a class="${activePath == '/news' || activePath == '/news-detail' ? 'active' : ''}" href="${ctx}/news">Tin tức</a>
        <a class="${activePath == '/support' ? 'active' : ''}" href="${ctx}/support">Hỗ trợ</a>
        <a class="mobile-only-nav-link" href="${ctx}/wishlist"><i class="fa-regular fa-heart"></i> Sản phẩm yêu thích</a>
        <c:choose><c:when test="${not empty sessionScope.auth}"><a class="mobile-only-nav-link" href="${ctx}/settings"><i class="fa-regular fa-user"></i> Tài khoản của tôi</a></c:when><c:otherwise><a class="mobile-only-nav-link" href="${ctx}/login"><i class="fa-regular fa-user"></i> Đăng nhập / Đăng ký</a></c:otherwise></c:choose>
    </nav>

    <a class="kk-brand" href="${ctx}/home" aria-label="Celine Closet - Trang chủ">
        <strong>CELINE CLOSET</strong><small>OFFICEWEAR</small>
    </a>

    <nav class="kk-nav kk-nav-right" aria-label="Liên kết hỗ trợ">
        <a class="header-wishlist-link icon-only-header-link ${activePath == '/wishlist' ? 'active' : ''}" href="${ctx}/wishlist" aria-label="Sản phẩm yêu thích" title="Sản phẩm yêu thích">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/></svg>
            <b data-wishlist-count class="${wishlistCount > 0 ? '' : 'is-empty'}">${wishlistCount}</b>
        </a>
        <a class="header-cart-link icon-only-header-link ${activePath == '/cart' ? 'active' : ''}" href="${ctx}/cart" aria-label="Giỏ hàng" title="Giỏ hàng">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 8h12l1 13H5L6 8Z"/><path d="M9 8V6a3 3 0 0 1 6 0v2"/></svg>
            <c:if test="${cartCount > 0}"><b>${cartCount}</b></c:if>
        </a>
        <c:choose>
            <c:when test="${not empty sessionScope.auth}">
                <div class="header-account-dropdown">
                    <button class="header-account-link header-account-trigger" type="button" aria-label="Mở tài khoản ${sessionScope.auth.hoTen}" aria-haspopup="true">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>
                        <span>${sessionScope.auth.hoTen}</span><span class="account-chevron">⌄</span>
                    </button>
                    <div class="header-account-popover" role="menu">
                        <c:choose>
                            <c:when test="${sessionScope.auth.vaiTro == 'CUSTOMER'}">
                                <a href="${ctx}/settings" role="menuitem">Thông tin tài khoản</a>
                                <a href="${ctx}/orders" role="menuitem">Lịch sử mua hàng</a>
                                <a href="${ctx}/loyalty" role="menuitem">Điểm thưởng &amp; voucher</a>
                                <a href="${ctx}/wishlist" role="menuitem">Sản phẩm yêu thích</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${ctx}${sessionScope.auth.vaiTro == 'ADMIN' ? '/admin/dashboard' : '/admin/orders'}" role="menuitem">Khu vực quản lý</a>
                                <a href="${ctx}/settings" role="menuitem">Cài đặt tài khoản</a>
                            </c:otherwise>
                        </c:choose>
                        <a class="account-logout-link" href="${ctx}/logout" role="menuitem">Đăng xuất</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <a class="header-account-link" href="${ctx}/login" aria-label="Đăng nhập tài khoản">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg><span>Tài khoản</span>
                </a>
            </c:otherwise>
        </c:choose>
        <button type="button" class="header-search-toggle" data-search-toggle aria-label="Mở tìm kiếm">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
        </button>
    </nav>

    <form class="kk-search-panel" action="${ctx}/products" method="get" data-search-panel>
        <label for="headerSearch">Tìm kiếm sản phẩm</label>
        <div><input id="headerSearch" type="search" name="q" value="${param.q}" placeholder="Bạn đang tìm sản phẩm gì?"><button type="submit">Tìm</button></div>
    </form>
</header>
