import {backendUrl} from "./SabanConfig.ts";
import type {SearchResponse} from "../data/responses/ResponseInterfaces.ts";

/**
 * Finds count of matches across all languages
 * @param searchWord
 */
export async function findMatches(searchWord: string): Promise<Record<string, SearchResponse[]> | undefined> {
    const response = await fetch(`${backendUrl}/gui/search/${searchWord}`, {
        method: "GET",
        credentials: "include"
    });

    if (response.ok) {
        return await response.json();
    }

    console.error("Error fetching matches:", response.status);
}
