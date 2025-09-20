<script>
    import {Button} from "$lib/components/ui/button/index.ts";
    import {authLoading, loggedInUser} from "../data/stores/UserStore.ts";
    import {backendUrl} from "../util/SabanConfig.ts";

    let isMenuOpen = false;

    const toggleMenu = () => {
        isMenuOpen = !isMenuOpen;
    };

    async function logout() {
        const resp = await fetch(`${backendUrl}/user/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        if (resp.ok) {
            loggedInUser.set(null);
        }
    }

</script>

<nav class="bg-white shadow-sm fixed w-full z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
            <!-- Logo -->
            <div class="flex items-center sm:space-x-4">
                <a href="/" class="text-xl font-bold text-gray-900">Saban</a>

                <div class="hidden sm:flex sm:items-center sm:space-x-4">
                    <a href="/" class="text-gray-700 hover:text-gray-900">Home</a>
                    <a href="/about" class="text-gray-700 hover:text-gray-900">About</a>
                </div>
            </div>

            <!-- Mobile Menu Button (Hamburger) -->
            <div class="flex items-center sm:hidden">
                <!--todo mit icon button ersetzen-->
                <Button onclick={() => toggleMenu()} variant="outline">=</Button>
            </div>

            <div class="hidden sm:flex sm:items-center sm:space-x-4">
                {#if $authLoading}
                    <p></p>
                {:else }
                    {#if $loggedInUser}
                        <p> Hello {$loggedInUser.username}</p>
                        <Button onclick={logout} variant="outline">Logout</Button>
                    {:else}
                        <Button href="/login" variant="outline">Login</Button>
                        <Button href="/signup">Sign Up</Button>
                    {/if}
                {/if}
            </div>
        </div>
    </div>

    <!-- Mobile Menu (Dropdown) -->
    {#if isMenuOpen}
        <div class="sm:hidden">
            <div class="px-2 pt-2 pb-3 space-y-1">
                <a href="/" class="block px-3 py-2 text-gray-700 hover:text-gray-900">Home</a>
                <a href="/about" class="block px-3 py-2 text-gray-700 hover:text-gray-900">About</a>


                {#if $loggedInUser === null}
                    <Button href="/login" class="w-full" variant="outline">Login</Button>
                    <Button href="/signup" class="w-full">Sign Up</Button>
                {:else }
                    <Button onclick={logout} variant="outline">Logout</Button>
                {/if}
            </div>
        </div>
    {/if}
</nav>