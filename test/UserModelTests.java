import org.junit.*;
import java.util.*;
import play.test.*;
import play.libs.*;
import models.*;


public class UserModelTests extends UnitTest {
	
	@Before
	public void setup() {
		Fixtures.deleteAll();
	}
	
	/**
	 * Base User API
	 */
	@Test
	public void storeAndLoadUser() {
		// Create and save user
		new User("knifa@glasnost.us", "hunter2", "Knifa").save();
		
		// Fetch user
		User knifa = User.find("byEmail", "knifa@glasnost.us").first();
		
		// Tests
		assertNotNull(knifa);
		assertEquals("Knifa", knifa.name);
		assertEquals(Crypto.passwordHash("hunter2"), knifa.passwordHash);
	}
	
	/**
	 * Test login functionality of User class.
	 */
	@Test
	public void loginAsUser() {
		new User("knifa@glasnost.us", "hunter2", "Knifa").save();
		
		assertNotNull(User.login("knifa@glasnost.us", "hunter2"));
		assertNull(User.login("knifa@glasnost.us", "badpassword"));
		assertNull(User.login("baduser@glasnost.us", "hunter2"));
		assertNull(User.login("baduser@glasnost.us", "badpassword"));
	}
}
