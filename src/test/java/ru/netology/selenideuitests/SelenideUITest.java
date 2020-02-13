package ru.netology.selenideuitests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelenideUITest {
    private SelenideElement root = $("#root");
    private SelenideElement form = $("form");
    private SelenideElement cityField = form.$("input[placeholder=Город]");
    private SelenideElement dateField = form.$("input[placeholder='Дата встречи']");
    private SelenideElement nameField = form.$("input[name=name]");
    private SelenideElement phoneField = form.$("input[name=phone]");
    private SelenideElement checkbox = form.$(".checkbox[data-test-id=agreement]");
    private SelenideElement buttonNext = form.$(byText("Забронировать"));
    private SelenideElement body = $("body");
    private LocalDate currentDate;

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
        currentDate = LocalDate.now();
    }

    @Test
    @DisplayName("Корректный ввод после ошибочного ввода")
    void correctInputAfterIncorrect() {
        buttonNext.click();
        form.$("[data-test-id=city] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        cityField.setValue("Казань");
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("Неверно введена дата"));
        dateField.setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        buttonNext.click();
        form.$("[data-test-id=name] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        nameField.setValue("Нефедова Алена");
        buttonNext.click();
        form.$("[data-test-id=phone] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        phoneField.setValue("+79040402204");
        buttonNext.click();
        String color = form.$(".input_invalid .checkbox__text").getCssValue("color");
        assertEquals("rgba(255, 92, 92, 1)", color);
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("104 символа в поле ФИО")
    void input104CharsInNameField() {
        cityField.setValue("Казань");
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        for (int i = 0; i < 13; i++) {
            nameField.setValue("Проверка");
        }
        phoneField.setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        form.$("[data-test-id=name] .input__sub")
                .shouldHave(text("Имя и Фамилия указаные неверно"));
    }

    @Test
    @DisplayName("Минимальная дата + 1 день")
    void checkMinimalDatePlusOne() {
        cityField.setValue("Казань");
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nameField.setValue("Ким Даша");
        phoneField.setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("Минимальная дата + 1 год")
    void checkMinimalDatePlusYear() {
        cityField.setValue("Казань");
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nameField.setValue("Ким Даша");
        phoneField.setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("Раньше минимальной даты")
    void checkMinimalDate() {
        cityField.setValue("Казань");
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nameField.setValue("Ким Даша");
        phoneField.setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("на выбранную дату невозможен"));
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("на выбранную дату невозможен"));
    }

    @ParameterizedTest
    @DisplayName("Проверка позитивных сценариев")
    @CsvFileSource(resources = "/SelenideUITestPositiveData.csv", numLinesToSkip = 1)
    void checkHappyPathAppCardDeliveryService(String city, String name, String phone, String selector, String expected) {
        cityField.setValue(city);
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nameField.setValue(name);
        phoneField.setValue(phone);
        checkbox.click();
        buttonNext.click();
        root.$(selector).waitUntil(visible, 15000);
        root.$(selector).shouldHave(text("Встреча успешно забронирована на"));
    }

    @ParameterizedTest
    @DisplayName("Некорректный ввод даты")
    @CsvFileSource(resources = "/SelenideUITestIncorrectDate.csv", numLinesToSkip = 1)
    void checkIncorrectInputDate(String city, String date, String name, String phone, String selector, String expected) {
        cityField.setValue(city);
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(date);
        nameField.setValue(name);
        phoneField.setValue(phone);
        checkbox.click();
        buttonNext.click();
        form.$(selector).shouldHave(text(expected));
    }

    @ParameterizedTest
    @DisplayName("Проверка негативных сценариев")
    @CsvFileSource(resources = "/SelenideUITestWrongPath.csv", numLinesToSkip = 1)
    void checkWrongPathAppCardDeliveryService(String city, String name, String phone, String selector, String expected) {
        cityField.setValue(city);
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        dateField.setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nameField.setValue(name);
        phoneField.setValue(phone);
        checkbox.click();
        buttonNext.click();
        form.$(selector).shouldHave(text(expected));
    }

    @Test
    @DisplayName("Проверка всплывающего окна Список городов")
    void checkCitiesPopup() {
        cityField.setValue("Ка");
        ElementsCollection cities = body.$$(".popup_height_adaptive .menu .menu-item__control");
        cities.get(4).click();
        cityField.shouldHave(value("Казань"));
    }

    @Test
    @DisplayName("Проверка всплывающего окна Календарь")
    void checkCalendarPopup() {
        cityField.setValue("Казань");
        form.$(".icon_name_calendar").click();
        SelenideElement calendar = body.$(".popup_padded");
        String dayStateCurrent = calendar.$(".calendar__day_state_current").getAttribute("data-day");
        long unixDayStateCurrent = Long.parseLong(dayStateCurrent);
        long unixFourDays = 345600000;
        long targetUnixDay = unixDayStateCurrent + unixFourDays;
        String targetDay = String.valueOf(targetUnixDay);
        String selector = "[data-day='" + targetDay + "']";

        while (true) {
            ElementsCollection dates = calendar.$$("[data-day]");
            if (searchTargetDate(dates, targetDay)) {
                calendar.$(selector).click();
                break;
            } else {
                calendar.$("[data-step='1']").click();
            }
        }

        nameField.setValue("Ким Даша");
        phoneField.setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    boolean searchTargetDate(ElementsCollection dates, String targetDay) {
        for (SelenideElement date : dates) {
            String search = (date.getAttribute("data-day"));
            if (search.equals(targetDay)) {
                return true;
            }
        }
        return false;
    }


}