import {backendUrl} from "../util/SabanConfig.ts";
import {authLoading, loggedInUser, type User} from "../data/stores/UserStore.ts";

export const load = async ({fetch}) => {
    authLoading.set(true);

    const response = await fetch(`${backendUrl}/user/login-check`, {
        credentials: "include",
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (response.ok) {
        const user: User = await response.json();
        loggedInUser.set(user);
    } else {
        loggedInUser.set(null);
    }

    authLoading.set(false);
};