package main

import "fmt"

func main() {
	dealerA := "A"
	dealerB := "B"
	dealerC := "C"
	printDealers(dealerA, dealerB, dealerC)

	dealers := []string{dealerA, dealerB, dealerC}
	printDealers(dealers ...)
}

func printDealers(dealers ...string) {
	for _, dealer := range dealers {
		fmt.Println(dealer)
	}
}