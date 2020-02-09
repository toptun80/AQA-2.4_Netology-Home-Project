package ru.netology.SelenideUITests;

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
    private SelenideElement form;
    private SelenideElement root;
    private SelenideElement cityField;
    private SelenideElement dateField;
    private SelenideElement nameField;
    private SelenideElement phoneField;
    private SelenideElement checkbox;
    private SelenideElement buttonNext;
    private LocalDate currentDate;
    SelenideElement body;

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
        root = $("#root");
        form = $("form");
        cityField = form.$("input[placeholder=Город]");
        dateField = form.$("input[placeholder='Дата встречи']");
        nameField = form.$("input[name=name]");
        phoneField = form.$("input[name=phone]");
        checkbox = form.$(".checkbox[data-test-id=agreement]");
        buttonNext = form.$(byText("Забронировать"));
        currentDate = LocalDate.now();
        body = $("body");
    }

    @Test
    @DisplayName("Корректный ввод после ошибочного ввода")
    void correctInputAfterIncorrect() {
        buttonNext.click();
        form.$("[data-test-id=city] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("Неверно введена дата"));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        buttonNext.click();
        form.$("[data-test-id=name] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        element(nameField).setValue("Нефедова Алена");
        buttonNext.click();
        form.$("[data-test-id=phone] .input__sub").shouldHave(text("Поле обязательно для заполнения"));
        element(phoneField).setValue("+79040402204");
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
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        for (int i = 0; i < 13; i++) {
            element(nameField).setValue("Проверка");
        }
        element(phoneField).setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        form.$("[data-test-id=name] .input__sub")
                .shouldHave(text("Имя и Фамилия указаные неверно"));
    }

    @Test
    @DisplayName("Минимальная дата + 1 день")
    void checkMinimalDatePlusOne() {
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(4).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        element(nameField).setValue("Ким Даша");
        element(phoneField).setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("Минимальная дата + 1 год")
    void checkMinimalDatePlusYear() {
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        element(nameField).setValue("Ким Даша");
        element(phoneField).setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        root.$(".notification").waitUntil(visible, 15000);
        root.$(".notification").shouldHave(text("Встреча успешно забронирована на"));
    }

    @Test
    @DisplayName("Раньше минимальной даты")
    void checkMinimalDate() {
        element(cityField).setValue("Казань");
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        element(nameField).setValue("Ким Даша");
        element(phoneField).setValue("+79040402204");
        checkbox.click();
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("на выбранную дату невозможен"));
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        buttonNext.click();
        form.$(".input[data-test-id=date], .input_invalid .input__sub").shouldHave(text("на выбранную дату невозможен"));
    }

    @ParameterizedTest
    @DisplayName("Проверка позитивных сценариев")
    @CsvFileSource(resources = "/SelenideUITestPositiveData.csv", numLinesToSkip = 1)
    void checkHappyPathAppCardDeliveryService(String city, String name, String phone, String selector, String expected) {
        element(cityField).setValue(city);
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        element(nameField).setValue(name);
        element(phoneField).setValue(phone);
        checkbox.click();
        buttonNext.click();
        root.$(selector).waitUntil(visible, 15000);
        root.$(selector).shouldHave(text("Встреча успешно забронирована на"));
    }

    @ParameterizedTest
    @DisplayName("Некорректный ввод даты")
    @CsvFileSource(resources = "/SelenideUITestIncorrectDate.csv", numLinesToSkip = 1)
    void checkIncorrectInputDate(String city, String date, String name, String phone, String selector, String expected) {
        element(cityField).setValue(city);
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(date);
        element(nameField).setValue(name);
        element(phoneField).setValue(phone);
        checkbox.click();
        buttonNext.click();
        form.$(selector).shouldHave(text(expected));
    }

    @ParameterizedTest
    @DisplayName("Проверка негативных сценариев")
    @CsvFileSource(resources = "/SelenideUITestWrongPath.csv", numLinesToSkip = 1)
    void checkWrongPathAppCardDeliveryService(String city, String name, String phone, String selector, String expected) {
        element(cityField).setValue(city);
        element(dateField).sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        element(dateField).setValue(currentDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        element(nameField).setValue(name);
        element(phoneField).setValue(phone);
        checkbox.click();
        buttonNext.click();
        form.$(selector).shouldHave(text(expected));
    }

    @Test
    @DisplayName("Проверка всплывающего окна Список городов")
    void checkCitiesPopup() {
        element(cityField).setValue("Ка");
        ElementsCollection cities = body.$$(".popup_height_adaptive .menu .menu-item__control");
        element(cities.get(4)).click();
        element(cityField).shouldHave(value("Казань"));
    }

    @Test
    @DisplayName("Проверка всплывающего окна Календарь")
    void checkCalendarPopup() {
        element(cityField).setValue("Казань");
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

        element(nameField).setValue("Ким Даша");
        element(phoneField).setValue("+79040402204");
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