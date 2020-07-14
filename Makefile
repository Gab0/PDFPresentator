build:
	mvn clean
	mvn package -U -e
	cp target/pdfpresenter-1.0-SNAPSHOT.jar ./pdfpresenter.jar
run:
	java -jar pdfpresenter.jar

