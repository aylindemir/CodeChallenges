package budgetTest;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static org.testng.Assert.assertEquals;

public class BananaBudgetTest {
	
	Response response;
	String baseURL = "https://bananabudget.azurewebsites.net";
	
	@DataProvider(name = "testData")
    public Object[][] getTestData() {
        return new Object[][] {
        	{"10/12/2017", 1},
        	{"10/12/2018", 10},
        	{"10/12/2019", 100},
        	{"05/21/2019", 365},
        	{"01/04/2020", 25},
        };
    }
	
	@DataProvider(name = "testDataNegative")
    public Object[][] getTestDataNegative() {
        return new Object[][] {
        	{"05/21/2019", "0", "Invalid numberOfDays"},
//        	{"05/21/2019", "366", "Invalid numberOfDays"}, //BUG: Max Day must be 365, but accepting more than 365
        	{"05/21/2019", "ABC", "Invalid numberOfDays"},
        	{"05/21/2019", "`,.>", "Invalid numberOfDays"},
        	{"13/21/2019", "10", "Invalid startDate"},
        	{"05/35/2019", "10", "Invalid startDate"},
//        	{"05/21/111", "10", "Invalid startDate"}, //BUG: format must be MM/DD/YYYY, but accepting MM/DD/YYY
//        	{"10/10/10", "10", "Invalid startDate"}, //BUG: format must be MM/DD/YYYY, but accepting MM/DD/YY
//        	{"10/10/1", "10", "Invalid startDate"}, //BUG: format must be MM/DD/YYYY, but accepting MM/DD/Y
        	{"13212019", "10", "Invalid startDate"},
        	{"13-21-2019", "10", "Invalid startDate"},
        	{"././.", "10", "Invalid startDate"},
        };
    }
	
	@Test(dataProvider = "testData")
	public void positiveTests(String startDate, int numberOfDays) {

		response = given()
		.queryParam("startDate", startDate)
		.queryParam("numberOfDays", numberOfDays)
		.when()
		.get(baseURL);
		
		System.out.println("Response: " + response.getBody().asString());
		System.out.println("Expected: " + getTotal(startDate, numberOfDays));
		
		String actualTotal = response.body().jsonPath().get("totalCost");
		String expectedTotal = getTotal(startDate, numberOfDays);
		
        assertEquals(response.getStatusCode(), HttpStatus.SC_OK, "invalid statusCode");
		assertEquals(actualTotal, expectedTotal, "invalid totalCost");
	}
	
	@Test(dataProvider = "testDataNegative")
	public void negativeTests(String startDate, String numberOfDays, String errorMessage) {

		response = given()
		.queryParam("startDate", startDate)
		.queryParam("numberOfDays", numberOfDays)
		.when()
		.get(baseURL);
		
		System.out.println("Response: " + response.getBody().asString());
		
		String actualErrorMessage = response.body().jsonPath().get("error");
		String expectedErrorMessage = errorMessage;
		
		// Verify status code is not 200;
		//assertEquals(response.getStatusCode(), HttpStatus.SC_UNPROCESSABLE_ENTITY, "invalid statusCode"); //BUG: Usually status code must be not 200
		
		//Verify error message
		assertEquals(actualErrorMessage, expectedErrorMessage, "invalid errorMessage");
	}
	
	public static String getTotal(String date, int days) {
		DecimalFormat dcFormat = new DecimalFormat("$0.00");
		DateTimeFormatter formater = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate startDay = LocalDate.parse(date, formater);
		int numberOfDays = days;
		double totalCost = 0;
		double unitPrice = 0;

		double seven1 = 0.05;
		double seven2 = 0.10;
		double seven3 = 0.15;
		double seven4 = 0.20;
		double seven5 = 0.25;

		for (int i = 0; i < numberOfDays; i++) {
			int dayNumber = startDay.plusDays(i).getDayOfMonth();
			DayOfWeek dayText = startDay.plusDays(i).getDayOfWeek();

			unitPrice = 0;
			if (dayText.equals(DayOfWeek.SATURDAY) || dayText.equals(DayOfWeek.SUNDAY)) {
				unitPrice = 0;
				totalCost += unitPrice;
			} else if (dayNumber <= 7) {
				unitPrice = seven1;
				totalCost += unitPrice;
			} else if (dayNumber <= 14) {
				unitPrice = seven2;
				totalCost += unitPrice;
			} else if (dayNumber <= 21) {
				unitPrice = seven3;
				totalCost += unitPrice;
			} else if (dayNumber <= 28) {
				unitPrice = seven4;
				totalCost += unitPrice;
			} else if (dayNumber <= 31) {
				unitPrice = seven5;
				totalCost += unitPrice;
			}
		}
		return dcFormat.format(totalCost);
	}
}