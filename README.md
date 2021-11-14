# Arbitrage Scanner

![Coverage](.github/badges/jacoco.svg)

Arbitrage Scanner App between Binance and Coinbase Pro exchanges.
Uses Scheduler to perform two tasks:
- Every hour, fetch all trading pairs with USD(T) as a quote currency which exists in both binance and coinbase pro,
and save to the H2 db if it's not exist in the db
- Every 30 seconds, for all known pairs from the db get last price using ticker endpoints, calculate base points(bips), 
and notify if the difference is more than 50 points  

## Endpoints used:

### Binance

- https://api.binance.com/api/v3/exchangeInfo
- https://api.binance.com/api/v3/ticker/price

### Coinbase Pro

- https://api.exchange.coinbase.com/products
- https://api.exchange.coinbase.com/products/%s/ticker
