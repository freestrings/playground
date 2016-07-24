package main

import "fmt"

func main() {
	p := new(person)
	fmt.Println(p.talk())
	fmt.Println(p.walk())

	o := new(policeOfficer)
	fmt.Println(o.talk())
	fmt.Println(o.walk())
	o.badgeNumber = 1000
	fmt.Println(o.badgeNumber)
	fmt.Println(o.run())

	fmt.Println(run(11))

}

type person struct {
	firstname string
	lastname string
}

type policeOfficer struct {
	badgeNumber int
	precint string
}

type behaviours interface {
	talk() string
	walk() int
	run() int
}

// person impl
func (p *person) talk() string {
	return "hi there"
}

func (p *person) walk() int {
	return 10
}

// officer impl
func (o *policeOfficer) talk() string {
	return "Hi here"
}

func (o *policeOfficer) walk() int {
	return 20
}

// func [param list] [interface func name] [interface func return name]
func (o *policeOfficer) run() int {
	return o.badgeNumber
}


// regular function
// func [name] [param list] [return type]
func run(s int) int {
	return s+10
}