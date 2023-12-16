<%@ page import="java.util.List" %>
<%@ page import="model.Gold" %>
<%@ page import="db.Dao" %>

<!DOCTYPE html>
<%@ page contentType="text/html;charsetUTF-8" language="java" pageEncoding="utf-8"%>
<html lang="xzz">
<meta http-equiv="Content-Type" charset="UTF-8">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
            href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600&display=swap"
            rel="stylesheet"
    />
    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css"
    />
    <link rel="stylesheet" href="css/style.css" />
    <link rel="stylesheet" href="css/reset.css" />
    <title>GiaVangOnline</title>
</head>
<body>
<header class="header">
    <div class="container header__inner">
        <a href="index.html" class="logo">
            <span class="logo__text">Giá Vàng </span>Online
        </a>
        <nav class="header__menu">
            <ul class="header__menu-list">
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link">Home</a>
                </li>
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link">Mix & Match</a>
                </li>
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link">Cẩm Nang Cưới</a>
                </li>
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link">Kiến Thức Trang Sức</a>
                </li>
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link">Tin Tức</a>
                </li>
                <li class="header__menu-item">
                    <a href="#" class="header__menu-link header__menu-search"
                    >Search <i class="fa-solid fa-magnifying-glass"></i
                    ></a>
                </li>
            </ul>
        </nav>
    </div>
</header>
<div class="wrapper">
    <section class="banner">
        <div class="container">
            <h1 class="banner__heading">GIÁ VÀNG PNJ, SJC MỚI NHẤT HÔM NAY</h1>
            <img src="img/banner.png" alt="" />
        </div>
    </section>

    <section class="locate">
        <div class="locate__title">Xem giá vàng tại:</div>
        <div class="dropdown">
            <div class="dropdow__select">
                <span class="select">Hồ Chí Minh</span>
                <i class="fa-solid fa-caret-down"></i>
            </div>
            <div class="dropdown-list">
                <div class="dropdown-list__item">Cần Thơ</div>
                <div class="dropdown-list__item">Hà Nội</div>
            </div>
        </div>
    </section>
    <%List<Gold> listGold = Dao.getNewData();
    %>
    <section class="table">

        <h1 class="table__time">CẬP NHẬT NGÀY: <%=listGold.get(0).getDate()%> <%=listGold.get(0).getTime()%></h1>
        <table>
            <thead>
            <tr>
                <th>Loại vàng | ĐVT: 1.000đ/Chỉ</th>
                <th>Giá mua</th>
                <th>Giá bán</th>
            </tr>
            </thead>
            <tbody>
            <% for (Gold g: listGold) {%>
            <tr>
                <td class="name"> <%=g.getType()%> </td>
                <td><%=g.getBuying()%></td>
                <td><%=g.getSelling()%></td>
            </tr>
            <%}%>
            </tbody>
        </table>
    </section>
    <footer class="footer">
        <div class="container footer__container">
            <div class="footer__item">
                <a href="index.html" class="logo__footer">
                    <span class="logo__text">Giá Vàng </span>Online
                </a>
            </div>
            <div class="footer__item">
                <a href="#" class="footer__link"
                >Công Ty Cổ Phần Vàng Bạc Đá Quý Phú Nhuận</a
                >
            </div>
            <div class="footer__item">
                <a href="#" class="footer__link"
                >170E Phan Đăng Lưu, P.3, Q.Phú Nhuận, TP.Hồ Chí Minh</a
                >
            </div>
            <div class="footer__item">
                <a href="#" class="footer__link"
                >Hotline: 1800545457</a
                >
            </div>
        </div>
    </footer>
</div>
</body>
</html>
