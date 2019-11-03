To start the app:

1) mvn clean install

2) java -jar target/payment-app-1.0-SNAPSHOT.jar


Jetty webservice starts at localhost:8080/payments

Available data:

Accounts:

  ACCOUNT_A with 1000 EUR, 1000 USD and 1000 GBP
  
  ACCOUNT_B (empty)
  
  ACCOUNT_C (empty)
  
  ACCOUNT_D (empty)
  
  
  Rest examples
  
  1. Pay:
  
  POST localhost:8080/payments/pay

{
	"debitAccountNumber" : "ACCOUNT_A",
	"creditAccountNumber" : "ACCOUNT_B",
	"amount" : 100,
	"currencyCode" : "EUR",
	"fillOrKill" : true
}

2. Get payment status:

GET localhost:8080/payments/paymentstatus/1

3. Get account balance:

GET localhost:8080/payments/accountbalance?accountNumber=ACCOUNT_A&currencyCode=EUR
