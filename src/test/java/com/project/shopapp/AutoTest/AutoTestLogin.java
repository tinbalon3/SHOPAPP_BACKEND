package com.project.shopapp.AutoTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

import static org.junit.Assert.assertEquals;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
public class AutoTestLogin {
    WebDriver driver;

    @Before
    public void setUp() {
        // Đặt đường dẫn đến msedgedriver.exe
        System.setProperty("webdriver.edge.driver", "C:\\Users\\Admin\\OneDrive\\Documents\\GitHub\\shoppapp_backend\\src\\test\\java\\com\\project\\shopapp\\AutoTest\\edgedriver_win64\\msedgedriver.exe"); // Chỉnh sửa đường dẫn đến msedgedriver.exe

        // Khởi tạo EdgeDriver
        driver = new EdgeDriver();
    }

    @Test
    @DisplayName("Đăng nhập thành công với vai trò người dùng")
    public void login_001() {
        driver.get("http://localhost:4200/login");

        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled@gmail.com");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));

        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Chờ tối đa 10 giây
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/"));

    }
    @Test
    @DisplayName("Đăng nhập thành công với vai trò quản trị viên thành công")
    public void login_002() {
        driver.get("http://localhost:4200/login");

        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("admin@dev.vna.com");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("admin");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));

        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Chờ tối đa 10 giây
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/admin/products"));

    }

    @Test
    @DisplayName("Đăng nhập thất bại vì sai mật khẩu")
    public void login_003() {
        driver.get("http://localhost:4200/login");

        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled@gmail.com");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("1234567");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));

        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Chờ tối đa 10 giây
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/login"));

    }

    @Test
    @DisplayName("Đăng nhập thất bại vì sai tài khoản")
    public void login_004() {
        driver.get("http://localhost:4200/login");

        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled123@gmail.com");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));

        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Chờ tối đa 10 giây
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/login"));

    }

    @Test
    @DisplayName("Đăng nhập thất bại vì tài khoản bị khóa")
    public void login_005() {
        driver.get("http://localhost:4200/login");

        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userblocked@gmail.com");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");
        try {
            Thread.sleep(1000); // 1 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));

        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Chờ tối đa 10 giây
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/login"));

    }
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
