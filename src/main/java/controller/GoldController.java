package controller;

import dao.GoldDAO;
import model.Gold;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/Home")
public class GoldController extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public GoldController() {
    super();
    // TODO Auto-generated constructor stub
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    request.setCharacterEncoding("UTF-8");
    GoldDAO goldDao = new GoldDAO();
    List<Gold> list = goldDao.getAllGold();
    request.setAttribute("listAllGold", list);
    request.getRequestDispatcher("/index.jsp").forward(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    doGet(request, response);
  }


}