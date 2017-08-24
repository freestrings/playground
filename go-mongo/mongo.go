//
//http://labix.org/mgo
//go run mongo.go
//
package main

import (
	"log"
	"strconv"
	"time"

	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

type Customer struct {
	Id        string
	Name      string
	Companies []struct {
		CompanyName string
	}
}

func main() {
	session, err := mgo.Dial("localhost")
	if err != nil {
		panic(err)
	}
	defer session.Close()

	c := session.DB("testa").C("customers")
	start := time.Now()

	for i := 0; i < 1000; i++ {
		result := Customer{}
		err = c.Find(bson.M{"name": "Customer" + strconv.Itoa(i)}).One(&result)
		if err != nil {
			log.Fatal(err)
		}

		//log.Println("Customer" + strconv.Itoa(i), result.Name)
	}

	elapsed := time.Since(start)

	log.Printf("%s", elapsed)
}
