package ru.netology.SelenideUITests;

import com.codeborne.selenide.SelenideElement;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelenideUITest {
    private SelenideElement form;
    private WebElement cityField;
    private WebElement dateField;
    private WebElement nameField;
    private WebElement phoneField;
    private WebElement checkbox;
    private WebElement buttonNext;
    private LocalDate currentDate;

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
        form = $("form");
        cityField = form.$("input[placeholder=Город]");
        dateField = form.$("input[placeholder='Дата встречи']");
        nameField = form.$("input[name=name]");
        phoneField = form.$("input[name=phone]");
        checkbox = form.$("label.checkbox[data-test-id=agreement]");
        buttonNext = form.$(byText("Забронировать"));
        currentDate = LocalDate.now();
    }

    @Test
    @DisplayName("Корректный ввод после ошибочного ввода")
    void correctInputAfterIncorrect() {
        buttonNext.click();
        form.$("span.input_invalid[data-test-id=city] span.input__sub")
                .shouldHave(text("Поле обязательно для заполнения"));
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        buttonNext.click();
        form.$("span.calendar-input__custom-control[data-test-id=date], span.input_invalid span.input__sub")
                .shouldHave(text("Неверно введена дата"));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        buttonNext.click();
        form.$("span.input_invalid[data-test-id=name] span.input__sub")
                .shouldHave(text("Поле обязательно для заполнения"));
        element(nameField).setValue("Нефедова Алена");
        buttonNext.click();
        form.$("span.input_invalid[data-test-id=phone] span.input__sub")
                .shouldHave(text("Поле обязательно для заполнения"));
        element(phoneField).setValue("+79040402204");
        buttonNext.click();
        String color = form.$("label.input_invalid span.checkbox__text").getCssValue("color");
        assertEquals("rgba(255, 92, 92, 1)", color);
        checkbox.click();
        buttonNext.click();
        SelenideElement popUpSuccessfully = $("div.notification")
                .waitUntil(visible, 15000);
        popUpSuccessfully.shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("104 символа в поле ФИО")
    void input104CharsInNameField() {
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        for (int i = 0; i < 13; i++) {
            element(nameField).setValue("Проверка");
        }
        element(phoneField).setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        form.$("span.input_invalid[data-test-id=name] span.input__sub")
                .shouldHave(text("Имя и Фамилия указаные неверно"));
    }
}