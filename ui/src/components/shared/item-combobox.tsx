"use client";

import { Check, ChevronsUpDown, Loader2 } from "lucide-react";
import * as React from "react";

import { Button } from "@/components/ui/button";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";

export interface ItemOption {
     id: number;
     sku: string;
     name: string;
}

interface ItemComboboxProps {
     value: string;
     onChange: (sku: string, itemId?: number) => void;
     placeholder?: string;
     disabled?: boolean;
     className?: string;
}

/**
 * Combobox component for selecting items by SKU.
 * Pre-loads items on mount and searches backend as user types.
 */
export function ItemCombobox({
     value,
     onChange,
     placeholder = "Select item...",
     disabled = false,
     className,
}: ItemComboboxProps) {
     const [open, setOpen] = React.useState(false);
     const [items, setItems] = React.useState<ItemOption[]>([]);
     const [loading, setLoading] = React.useState(false);
     const [searchQuery, setSearchQuery] = React.useState("");
     const debounceTimerRef = React.useRef<NodeJS.Timeout | null>(null);

     // Initial load of items
     React.useEffect(() => {
          const loadInitialItems = async () => {
               setLoading(true);
               try {
                    const response = await fetch("/api/items/search?q=");
                    if (response.ok) {
                         const data = await response.json();
                         setItems(
                              data.map((item: { id: number; sku: string; name: string }) => ({
                                   id: item.id,
                                   sku: item.sku,
                                   name: item.name,
                              }))
                         );
                    }
               } catch (error) {
                    console.error("Failed to load items:", error);
               } finally {
                    setLoading(false);
               }
          };

          loadInitialItems();
     }, []);

     // Debounced search
     const handleSearch = React.useCallback((query: string) => {
          setSearchQuery(query);

          // Clear existing timer
          if (debounceTimerRef.current) {
               clearTimeout(debounceTimerRef.current);
          }

          // Set new debounce timer
          debounceTimerRef.current = setTimeout(async () => {
               setLoading(true);
               try {
                    const response = await fetch(`/api/items/search?q=${encodeURIComponent(query)}`);
                    if (response.ok) {
                         const data = await response.json();
                         setItems(
                              data.map((item: { id: number; sku: string; name: string }) => ({
                                   id: item.id,
                                   sku: item.sku,
                                   name: item.name,
                              }))
                         );
                    }
               } catch (error) {
                    console.error("Failed to search items:", error);
               } finally {
                    setLoading(false);
               }
          }, 300);
     }, []);

     // Cleanup timer on unmount
     React.useEffect(() => {
          return () => {
               if (debounceTimerRef.current) {
                    clearTimeout(debounceTimerRef.current);
               }
          };
     }, []);

     const selectedItem = items.find((item) => item.sku === value);

     return (
          <Popover open={open} onOpenChange={setOpen}>
               <PopoverTrigger asChild>
                    <Button
                         variant="outline"
                         role="combobox"
                         aria-expanded={open}
                         disabled={disabled}
                         className={cn(
                              "w-full justify-between font-normal",
                              !value && "text-muted-foreground",
                              className
                         )}>
                         {value ? (
                              <span className="truncate">
                                   {selectedItem ? `${selectedItem.sku} - ${selectedItem.name}` : value}
                              </span>
                         ) : (
                              placeholder
                         )}
                         {loading ? (
                              <Loader2 className="ml-2 h-4 w-4 shrink-0 animate-spin opacity-50" />
                         ) : (
                              <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                         )}
                    </Button>
               </PopoverTrigger>
               <PopoverContent className="w-[var(--radix-popover-trigger-width)] p-0" align="start">
                    <Command shouldFilter={false}>
                         <CommandInput
                              placeholder="Search by SKU..."
                              value={searchQuery}
                              onValueChange={handleSearch}
                         />
                         <CommandList>
                              {loading ? (
                                   <div className="flex items-center justify-center py-6">
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        <span className="ml-2 text-sm text-muted-foreground">Searching...</span>
                                   </div>
                              ) : items.length === 0 ? (
                                   <CommandEmpty>
                                        {searchQuery ? "No items found matching your search." : "No items available."}
                                   </CommandEmpty>
                              ) : (
                                   <CommandGroup>
                                        {items.map((item) => (
                                             <CommandItem
                                                  key={item.id}
                                                  value={item.sku}
                                                  onSelect={() => {
                                                       onChange(item.sku, item.id);
                                                       setOpen(false);
                                                       setSearchQuery("");
                                                  }}>
                                                  <Check
                                                       className={cn(
                                                            "mr-2 h-4 w-4",
                                                            value === item.sku ? "opacity-100" : "opacity-0"
                                                       )}
                                                  />
                                                  <div className="flex flex-col">
                                                       <span className="font-medium">{item.sku}</span>
                                                       <span className="text-xs text-muted-foreground">
                                                            {item.name}
                                                       </span>
                                                  </div>
                                             </CommandItem>
                                        ))}
                                   </CommandGroup>
                              )}
                         </CommandList>
                    </Command>
               </PopoverContent>
          </Popover>
     );
}
