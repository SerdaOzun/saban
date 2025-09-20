<script lang="ts">
    import {Input} from "$lib/components/ui/input/index.js";
    import {Label} from "$lib/components/ui/label/index.js";
    import {Button} from "$lib/components/ui/button";
    import {findMatches} from "../util/Requests.ts";
    import {goto, replaceState} from "$app/navigation";
    import type {SearchResponse} from "../data/responses/ResponseInterfaces.ts";
    import {page} from "$app/state";

    let {largeBanner, searchTerm = $bindable(""), matches = $bindable({}), lastSearch = $bindable("")}: {
        largeBanner: boolean;
        searchTerm?: string;
        lastSearch?: string;
        matches?: Record<string, SearchResponse[]>
    } = $props()


    async function onSearch() {
        page.url.searchParams.delete("")

        if (page.url.pathname !== "/search") {
            await goto(`/search?searchText=${encodeURIComponent(searchTerm)}`)
        } else {
            let result = await findMatches(searchTerm)
            lastSearch = searchTerm;
            matches = result ?? {};
            page.url.searchParams.set('searchText', searchTerm)
            replaceState(page.url, page.state)
        }
    }

    async function handleKeydown(e: KeyboardEvent) {
        if (e.key === "Enter") {
            await onSearch()
        }
    }
</script>

{#if largeBanner === true}
    <div class="h-1/4 min-h-[200px] place-items-center bg-gray-300 w-full">
        <div class="h-2/3 place-content-center">
            <Label class="text-3xl">Saban. Pronuncation Dictionary</Label>
        </div>
        <div class="flex flex-row h-1/3 gap-2">
            <Input type="search" class="w-[400px]" onkeydown={handleKeydown} placeholder="Search pronunciation..."
                   bind:value={searchTerm}></Input>
            <Button type="submit" onclick={onSearch}>Search</Button>
        </div>
    </div>
{:else}
    <div class="place-items-center bg-gray-300 w-full">
        <div class="flex flex-row mt-2 mb-2 gap-2">
            <Input type="search" class="w-[400px]" onkeydown={handleKeydown} placeholder="Search pronunciation..."
                   bind:value={searchTerm}></Input>
            <Button type="submit" onclick={onSearch}>Search</Button>
        </div>
    </div>
{/if}
