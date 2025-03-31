# JavaCapture

JavaCapture is a Java-based application for enrolling fingerprints using a DigitalPersona fingerprint scanner. The application captures, processes, and stores fingerprint data in a MySQL database.

## Features

- Capture fingerprints from the DigitalPersona scanner.
- Process and serialize fingerprint data.
- Store fingerprint data in a MySQL database.
- User-friendly GUI for interacting with the fingerprint scanner and managing user data.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- MySQL database
- DigitalPersona fingerprint scanner
- DigitalPersona SDK

## Installation

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yish-mael/javacapture.git
   cd javacapture
   ```

2. **Set up the MySQL database:**
   - Create a MySQL database and user with the necessary privileges.
   - Update the database connection details in the `FingerprintEnroll.java` file:
     ```java
     conn = DriverManager.getConnection("jdbc:mysql://<your-db-host>:3306/<your-db-name>", "<your-db-user>", "<your-db-password>");
     ```

3. **Compile and run the application:**
   ```sh
   javac -cp ".:path/to/digitalpersona/sdk/*" src/FingerprintEnroll.java
   java -cp ".:path/to/digitalpersona/sdk/*" src/FingerprintEnroll
   ```

## Usage

1. **Run the application:**
   ```sh
   java -cp ".:path/to/digitalpersona/sdk/*" src/FingerprintEnroll
   ```

2. **Enroll fingerprints:**
   - Select a user from the list.
   - Click the "Start Capturing" button.
   - Follow the prompts to scan the right thumb, right index, left thumb, and left index fingerprints.
   - The fingerprints will be processed and stored in the database.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License

This project does not currently specify a license.

## Contact

For questions or issues, please open an issue on the [GitHub repository](https://github.com/yish-mael/javacapture).

---

Feel free to customize this README based on any additional details or specific requirements for your project.
