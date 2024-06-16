## Define a variable for the version of your Docker image
CI_REGISTRY_IMAGE = mikenguyen
VERSION = SNAPSHOT
# Define a variable for the name of your Docker image
IMAGE_NAME = sizer-influx-grafana
IMAGE_REPO = ${CI_REGISTRY_IMAGE}/${IMAGE_NAME}

all: build tag push
# 'make build' will build your Docker image
build:
	docker build -t $(IMAGE_REPO) .

tag:
	docker tag ${IMAGE_REPO} ${IMAGE_REPO}:${VERSION}

push:
	docker push ${IMAGE_REPO}:${VERSION}