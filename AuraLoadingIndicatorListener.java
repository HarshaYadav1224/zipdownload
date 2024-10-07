package com.testbuddie.core.listeners;
import com.testbuddie.core.web.selenium.Application;
import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AuraLoadingIndicatorListener implements WebDriverListener {
    private WebDriver driver;

    public AuraLoadingIndicatorListener(WebDriver driver) {
        this.driver = driver; // Adjust time as needed
    }

    // Before any click action
    @Override
    public void beforeClick(WebElement element) {
        waitForLoaderToDisappear(element);
    }

    // Before setting text in a field
    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend) {
        waitForLoaderToDisappear(element);
    }

    // Add other WebDriverListener methods as needed...

    private void waitForLoaderToDisappear(WebElement element) {
        try
        {
            try
            {
                FluentWait<WebDriver> wait= new FluentWait<>(driver).withTimeout(Duration.ofSeconds(2)).pollingEvery(Duration.ofMillis(10)).ignoring(NoSuchElementException.class);
                By locator= By.xpath("//app-loader/div[@class='loader']");
                wait.withMessage("Waiting for loading indicator to appears")
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));
                wait = new FluentWait<>(Application.getInstance().getDriver()).withTimeout(Duration.ofSeconds(60)).pollingEvery(Duration.ofMillis(100)).ignoring(NoSuchElementException.class);;
                wait.withMessage("Waiting for loading indicator to disappears")
                        .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            }catch (Exception exception){
            }
        }catch (Exception exception){
        }
    }
}

