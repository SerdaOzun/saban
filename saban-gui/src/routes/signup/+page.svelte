<script>
    import {Label} from "@/components/ui/label/index.ts";
    import {Input} from "@/components/ui/input/index.ts";
    import {Button} from "@/components/ui/button/index.ts";
    import {backendUrl} from "../../util/SabanConfig.ts";

    let username = "";
    let email = "";
    let password = "";
    let error = false;
    let errorMsg = "";

    const handleSubmit = async () => {
        const response = await fetch(`${backendUrl}/user/register`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                "username": username,
                "email": email,
                "password": password,
            })
        });

        if (!response.ok) {
            error = true;
            errorMsg = await response.text();
        } else {
            //Ask user to login
        }
    };
</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center bg-gray-50">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 class="text-2xl font-bold text-center mb-6">Sign Up</h2>

        <form on:submit|preventDefault={handleSubmit} class="space-y-6">
            <div>
                <Label for="username">Username</Label>
                <Input
                        id="username"
                        type="text"
                        bind:value={username}
                        placeholder="Enter your username"
                        required
                />
            </div>

            <div>
                <Label for="email">Email</Label>
                <Input
                        id="email"
                        type="email"
                        bind:value={email}
                        placeholder="Enter your email"
                        required
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
                />
            </div>

            <!-- Submit Button -->
            <Button type="submit" class="w-full">Sign Up</Button>
        </form>

        {#if error}
            <Label class="text-red-500">{errorMsg}</Label>
        {/if}

    </div>
</div>