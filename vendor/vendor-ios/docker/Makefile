SWIFT_VERSION ?= 4.2.4
IMAGE_NAME ?= swift-binary-parser-$(SWIFT_VERSION)

BASE_IMAGE = ibmcom/swift-ubuntu:$(SWIFT_VERSION)
DOCKER_DIR = swift-$(SWIFT_VERSION)

.PHONY: build
build: update_base_image
	docker build \
		-t $(IMAGE_NAME) \
		-f $(DOCKER_DIR)/Dockerfile $(DOCKER_DIR)

.PHONY: update_base_image
update_base_image:
	sed -i.bak -e 's~^FROM ibmcom.*$$~FROM $(BASE_IMAGE)~' $(DOCKER_DIR)/Dockerfile
	-@rm -f $(DOCKER_DIR)/Dockerfile.bak