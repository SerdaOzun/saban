import {writable} from "svelte/store";

export interface User {
    username: string;
    email: string;
    userId: string;
}

export const loggedInUser = writable<User | null>(null);
export const authLoading = writable<boolean>(true);