namespace java test

struct BreedInfoResponse {
  1: string name
  2: bool beautiful
}

service DogBreedService {
  BreedInfoResponse breedInfo(1: string breedName)
}
