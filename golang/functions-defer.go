package main

import (
	"fmt"
	"os"
)

func main() {
	file:= createFile("dealers.txt")
	defer closeFile(file)
	writeToFile(file, "A1 Auto")
}

func createFile(path string) *os.File {
	fmt.Println("createFile")
	file, err := os.Create(path)
	if err != nil {
		panic(err)
	}

	return file
}

func writeToFile(file *os.File, dealerName string) {
	fmt.Println("writing to file")
	fmt.Fprintln(file, dealerName)
}

func closeFile(file *os.File) {
	fmt.Println("closing file")
	file.Close()
}