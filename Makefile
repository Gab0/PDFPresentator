NAME=PdfPresentator.jar
build:
	mvn clean
	mvn package -U -e
	cp target/pdfpresenter-1.0-SNAPSHOT.jar ./${NAME}
run:
	java -jar ${NAME}

