export interface SearchResponse {
	word: string;
	wordId: number;
}

export interface PronunciationResult {
	username: string;
	word: string;
	url: string;
	createdAt: string;
}

export interface PronunciationRequest {
	id: number;
	text: string;
	language: string;
	languageId: number;
	requestedBy: string;
	createdAt: string;
}

export interface PaginatedPronunciationResponse {
	totalCount: number;
	data: Array<PronunciationRequest>;
}
