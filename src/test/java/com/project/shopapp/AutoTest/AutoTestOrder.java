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
import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
public class AutoTestOrder {
    WebDriver driver;

    @Before
    public void setUp() {
        // Đặt đường dẫn đến msedgedriver.exe
        System.setProperty("webdriver.edge.driver", "C:\\Users\\Admin\\OneDrive\\Documents\\GitHub\\shoppapp_backend\\src\\test\\java\\com\\project\\shopapp\\AutoTest\\edgedriver_win64\\msedgedriver.exe"); // Chỉnh sửa đường dẫn đến msedgedriver.exe

        // Khởi tạo EdgeDriver
        driver = new EdgeDriver();
    }

    @Test
    @DisplayName("Đăng nhập thành công với vai trò người dùng và chọn sản phẩm, thêm vào giỏ hàng, điền thông tin thanh toán và chuyển sang trang thanh toán thành công")
    public void order_001() {
        // Mở trang đăng nhập
        driver.get("http://localhost:4200/login");

        // Điền thông tin đăng nhập
        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");

        // Nhấn nút đăng nhập
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));
        loginButton.click();

        // Chờ chuyển hướng tới trang chính
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Chọn sản phẩm với thẻ h5[ng-reflect-router-link='/products/1']

        WebElement productLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h5[ng-reflect-router-link='/products/1']")));
        productLink.click();

        WebElement buynowButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='btn-success']")));
        buynowButton.click();

        WebElement muangay = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='w-100']")));
        muangay.click();

        WebElement termCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='termsCheckbox']")));
//        if (!termCheckbox.isSelected()) {
//            termCheckbox.click();
//        }
        Actions actions = new Actions(driver);
        actions.moveToElement(termCheckbox).click().perform();

        WebElement addressInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='shippingStreet']")));
        addressInput.sendKeys("Dương Bá Trạc, Quận 8");

        WebElement checkoutButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class*='ezy']")));
//        checkoutButton.click();
        Actions actions_1 = new Actions(driver);
        actions_1.moveToElement(checkoutButton).click().perform();


        wait.until(ExpectedConditions.urlContains("https://sandbox.vnpayment.vn/paymentv2/Ncb/Transaction"));



    }
    @Test
    @DisplayName("Đăng nhập thành công với vai trò người dùng và chọn sản phẩm, thêm vào giỏ hàng, điền thông tin thanh toán và thanh toán thành công")
    public void order_002() {
        // Mở trang đăng nhập
        driver.get("http://localhost:4200/login");

        // Điền thông tin đăng nhập
        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");

        // Nhấn nút đăng nhập
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));
        loginButton.click();

        // Chờ chuyển hướng tới trang chính
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Chọn sản phẩm với thẻ h5[ng-reflect-router-link='/products/1']

        WebElement productLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h5[ng-reflect-router-link='/products/1']")));
        productLink.click();

        WebElement buynowButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='btn-success']")));
        buynowButton.click();

        WebElement muangay = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='w-100']")));
        muangay.click();

        WebElement termCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='termsCheckbox']")));

        Actions actions = new Actions(driver);
        actions.moveToElement(termCheckbox).click().perform();

        WebElement addressInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='shippingStreet']")));
        addressInput.sendKeys("Dương Bá Trạc, Quận 8");

        WebElement checkoutButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class*='ezy']")));
//        checkoutButton.click();
        Actions actions1 = new Actions(driver);
        actions1.moveToElement(checkoutButton).click().perform();

        WebElement numberInput = driver.findElement(By.cssSelector("input[id*='number']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", numberInput, "9704198526191432198");

        WebElement tentheinput = driver.findElement(By.cssSelector("input[id='cardHolder']"));
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].value = arguments[1];", tentheinput, "NGUYEN VAN A");


        WebElement ngayinput = driver.findElement(By.cssSelector("input[id='cardDate']"));
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("arguments[0].value = arguments[1];", ngayinput, "715");




        WebElement tieptucButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("html > body > div:nth-of-type(2) > div:nth-of-type(2) > div > div > div:nth-of-type(2) > div > div > div > div:nth-of-type(3) > div > div:nth-of-type(2) > form > div > div:nth-of-type(3) > div:nth-of-type(2) > div:nth-of-type(2) > a > div > span")));
        tieptucButton.click();

        WebElement agreeButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[id='btnAgree']")));
        agreeButton.click();

        WebElement otpInput = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='otpvalue']")));
        otpInput.sendKeys("123456");

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/checkout-status?status=success"));



    }
    @Test
    @DisplayName("Đăng nhập thành công với vai trò người dùng và chọn sản phẩm, thêm vào giỏ hàng, điền thông tin thanh toán và thanh toán thất bại")
    public void order_003() throws InterruptedException {
        // Mở trang đăng nhập
        driver.get("http://localhost:4200/login");

        // Điền thông tin đăng nhập
        WebElement usernameField = driver.findElement(By.id("user_name"));
        usernameField.sendKeys("userenabled@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123456");

        // Nhấn nút đăng nhập
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@class, 'btn') and contains(@class, 'ezy__signin1_VjHlH1lB-btn-submit') and contains(@class, 'w-100')]"));
        loginButton.click();

        // Chờ chuyển hướng tới trang chính
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Chọn sản phẩm với thẻ h5[ng-reflect-router-link='/products/1']

        WebElement productLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h5[ng-reflect-router-link='/products/1']")));
        productLink.click();

        WebElement buynowButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='btn-success']")));
        buynowButton.click();

        WebElement muangay = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class$='w-100']")));
        muangay.click();

        WebElement termCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='termsCheckbox']")));

        Actions actions = new Actions(driver);
        actions.moveToElement(termCheckbox).click().perform();

        WebElement addressInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='shippingStreet']")));
        addressInput.sendKeys("Dương Bá Trạc, Quận 8");
        Thread.sleep(2000);
        WebElement checkoutButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class*='ezy']")));
        Actions actions1 = new Actions(driver);
        actions1.moveToElement(checkoutButton).click().perform();

        WebElement cancelButton = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-bs-target='#modalCancelPayment']")));

        Actions actions2 = new Actions(driver);
        actions2.moveToElement(cancelButton).click().perform();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/checkout-status?status=success"));



    }
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
