package main

import "fmt"

var shippingDays = 30
var shippingDaysPtr = new(int)

func main() {
	shippingDaysAdjustments(shippingDays)
	fmt.Println("after", shippingDays)

	shippingDaysAjdjustmanetsPtr(&shippingDays)
	fmt.Println("after ptr", shippingDays)
	fmt.Println("after ptr", &shippingDays)

	shipper := shipper{}
	shipper.id = 400
	shipper.name = "Pacific"
	shipperUpdates(&shipper)
	fmt.Println("shipper id", shipper.id)
	fmt.Println("shipper name", shipper.name)

	*shippingDaysPtr = 55
	shippingDaysAjdjustmanetsPtr(shippingDaysPtr)
	fmt.Println("after ptr", *shippingDaysPtr)
}

func shippingDaysAdjustments(shippingDays int) {
	shippingDays += 10
}

func shippingDaysAjdjustmanetsPtr(shippingDays *int) {
	*shippingDays += 10
}

func shipperUpdates(s *shipper) {
	s.id += 10
	s.name += " Inc."
}

type shipper struct {
	name string
	id   int
}