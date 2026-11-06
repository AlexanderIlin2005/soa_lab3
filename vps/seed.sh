#!/bin/bash
URL="${1:-http://127.0.0.1:38080/vehicle-service/vehicles}"

post() {
  curl -sk -o /dev/null -w '%{http_code} ' -X POST "$URL" -H 'Content-Type: application/json' -d "$1"
}

post '{"name":"Harley Fat Boy","coordinates":{"x":12,"y":-40},"enginePower":86.5,"numberOfWheels":2,"mileage":18400,"type":"CHOPPER","fuelType":"GASOLINE"}'
post '{"name":"Orange County Chopper","coordinates":{"x":300,"y":15},"enginePower":110,"numberOfWheels":2,"mileage":5300.5,"type":"CHOPPER","fuelType":"GASOLINE"}'
post '{"name":"Iron Horse","coordinates":{"x":-75,"y":220},"enginePower":95.2,"numberOfWheels":2,"mileage":null,"type":"CHOPPER","fuelType":null}'
post '{"name":"Electra Glide","coordinates":{"x":471,"y":0},"enginePower":78,"numberOfWheels":3,"mileage":42000,"type":"CHOPPER","fuelType":"ELECTRICITY"}'
post '{"name":"Robinson R44","coordinates":{"x":150,"y":-300},"enginePower":245,"numberOfWheels":null,"mileage":1200,"type":"HELICOPTER","fuelType":"GASOLINE"}'
post '{"name":"Mi-8","coordinates":{"x":-410,"y":88},"enginePower":1500,"numberOfWheels":3,"mileage":96000,"type":"HELICOPTER","fuelType":"GASOLINE"}'
post '{"name":"Bell 407","coordinates":{"x":64,"y":64},"enginePower":813,"numberOfWheels":null,"mileage":null,"type":"HELICOPTER","fuelType":null}'
post '{"name":"Yamaha MT-07","coordinates":{"x":5,"y":7},"enginePower":73.4,"numberOfWheels":2,"mileage":15000,"type":"MOTORCYCLE","fuelType":"GASOLINE"}'
post '{"name":"Ducati Monster","coordinates":{"x":-120,"y":-120},"enginePower":111,"numberOfWheels":2,"mileage":8700,"type":"MOTORCYCLE","fuelType":"GASOLINE"}'
post '{"name":"Zero SR/F","coordinates":{"x":200,"y":310},"enginePower":110,"numberOfWheels":2,"mileage":3100,"type":"MOTORCYCLE","fuelType":"ELECTRICITY"}'
post '{"name":"Velomobile","coordinates":{"x":0,"y":0},"enginePower":0.3,"numberOfWheels":3,"mileage":2500,"type":"MOTORCYCLE","fuelType":"MANPOWER"}'
post '{"name":"Millennium Falcon","coordinates":{"x":471,"y":1977},"enginePower":99999,"numberOfWheels":null,"mileage":1000000,"type":"SPACESHIP","fuelType":"ANTIMATTER"}'
post '{"name":"Buran","coordinates":{"x":-300,"y":1988},"enginePower":28000,"numberOfWheels":3,"mileage":null,"type":"SPACESHIP","fuelType":null}'
post '{"name":"Tesla Roadster","coordinates":{"x":120,"y":-47},"enginePower":615.5,"numberOfWheels":4,"mileage":null,"type":null,"fuelType":"ELECTRICITY"}'
post '{"name":"Old Cart","coordinates":{"x":-1,"y":-1},"enginePower":1,"numberOfWheels":4,"mileage":120,"type":null,"fuelType":"MANPOWER"}'
echo
