<script lang="ts">
    import {Label} from "@/components/ui/label";
    import {Input} from "@/components/ui/input";
    import {Button} from "@/components/ui/button";
    import {goto} from "$app/navigation";
    import {type User, loggedInUser} from "../../data/stores/UserStore.ts";
    import {backendUrl} from "../../util/SabanConfig.ts";

    let email = $state("");
    let password = $state("");

    let error = $state(false);
    let errorMsg = $state("");

    const handleSubmit = async () => {
        const response = await fetch(`${backendUrl}/user/login`, {
            method: 'POST',
            credentials: "include",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                'email': email,
                'password': password,
                'grant_type': 'password'
            })
        });

        if (response.ok) {
            const user: User = await response.json();
            loggedInUser.set(user);
            return await goto("/");
        }

        if (response.status === 401) {
            error = true;
            errorMsg = "Wrong email or password"
        } else if (!response.ok) {
            error = true;
            errorMsg = await response.text();
        }
    };


</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center bg-gray-50">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 class="text-2xl font-bold text-center mb-6">Login</h2>

        <form class="space-y-6">
            <div>
                <Label for="email">Email</Label>
                <Input
                        id="email"
                        type="email"
                        bind:value={email}
                        placeholder="Enter your email"
                        required
                        autocomplete="email"
                />
            </div>

            <div>
                <Label for="password">Password</Label>
                <Input
                        id="password"
                        type="password"
                        bind:value={password}
                        placeholder="Enter your password"
                        required
                        autocomplete="current-password"
                />
            </div>

            <Button type="submit" class="w-full" onclick={handleSubmit}>Login</Button>
        </form>
        {#if error}
            <Label class="text-red-500">{errorMsg}</Label>
        {/if}
    </div>
</div>